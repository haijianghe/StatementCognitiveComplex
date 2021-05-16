/**
 * 
 */
package parseSourceCode;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.CreationReference;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodReference;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodReference;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeMethodReference;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

/**  以行为单位计算复杂度，不考虑表达式语句和变量声明内的嵌套语句。
 *  此类作为各类语句的visit，找出该语句内的函数调用、逻辑运算符、局部变量、参数、类属性等。
 * @author Administrator
 *
 *Direct Known Subclasses:  表达式种类。
 * Annotation, ArrayAccess, ArrayCreation, ArrayInitializer, Assignment, BooleanLiteral, CastExpression, 
 * CharacterLiteral, ClassInstanceCreation, ConditionalExpression, FieldAccess, InfixExpression, 
 * InstanceofExpression, LambdaExpression, MethodInvocation, MethodReference, Name, NullLiteral, 
 * NumberLiteral, ParenthesizedExpression, PostfixExpression, PrefixExpression, StringLiteral, 
 * SuperFieldAccess, SuperMethodInvocation, ThisExpression, TypeLiteral, VariableDeclarationExpression

 */
public class JavaFragmentVisitor extends ASTVisitor{
	private int metric;  //当次计算出的认知复杂度
	List<String> identiferNames; //记录该语句的所有标识符。排除保留字、函数名、类名、方法名。
	List<String> invoMethods; //记录函数名、类名、方法名，将来要从identiferNames中删除它们。
	List<String> logicOperators;  //条件操作符，不允许重复。&& || !
	List<String> strayOperators;  //混合Infix,前缀表达式操作符，后缀表达式操作符，不允许重复。 ++ -- ~ ^ & | 
	
	public JavaFragmentVisitor()
	{
		metric = 0;
		identiferNames = new ArrayList<>();
		invoMethods = new ArrayList<>();
		logicOperators = new ArrayList<>();
		strayOperators = new ArrayList<>();
	}
	
	/*  调整认知复杂度，条件操作符种类数，前缀(后缀)表达式操作符种类数
	 * 条件操作符种类数，每种+1
	 * 前缀(后缀)表达式操作符种类数,拿种类数-1作为复杂度。
	 */
	public void adjustCognitiveComplexityWithInfixOperator()
	{
		int inphyletic = logicOperators.size();
		if( inphyletic>0 )
			metric += inphyletic*MetricCognitiveComplexity.Unit;
		int ppphyletic = strayOperators.size();
		if( ppphyletic>0 )
			metric += (ppphyletic-1)*MetricCognitiveComplexity.Unit;
	}
	/**类内部的方法调用，复杂度+2;     外部的方法调用，复杂度+3
	 * @param invocation 语句表达式内部包含MethodInvocation
	 * @return
	 */
	@Override
	public boolean visit(MethodInvocation invocation) {
		Expression  express = invocation.getExpression();
		if( express==null )
		{
			metric +=MetricCognitiveComplexity.InnerMethod; //类内部的方法调用，复杂度+2
			String imn = invocation.getName().getIdentifier(); //找出方法名。
			addInvoMethod(imn);//将来要从identiferNames排除此值。
		}
		else
		{
			metric +=MetricCognitiveComplexity.ExternMethod; //外部的方法调用，复杂度+3
			if( express instanceof QualifiedName )
			{
				String strItem = ((QualifiedName)express).getFullyQualifiedName();
				String[] strParsed = strItem.split("\\.");
				for( String parsed : strParsed )
					addInvoMethod(parsed);
				String imn = invocation.getName().getIdentifier(); //找出方法名。
				addInvoMethod(imn);
			}
			else if( express instanceof SimpleName )
			{
				String strItem = ((SimpleName)express).getIdentifier();
				addInvoMethod(strItem);//将来要从identiferNames排除此值。
				String imn = invocation.getName().getIdentifier(); //找出方法名。
				addInvoMethod(imn);
			}
			else
			{
				//TypeLiteral:格式    ( Type | void ) . class
				if( express instanceof TypeLiteral )
					addInvoMethod("class");
				//一个一个地找出express的类型，太麻烦，简单地考虑用.划分。
				String strItem = express.toString();
				String[] strParsed = strItem.split("\\.");
				for( String parsed : strParsed )
					addInvoMethod(parsed);
			}
		}
		return true; //用true识别嵌套。
	}
	/**父类方法的调用，复杂度+2 （为简化引用类型的复杂度，可能调为3）
	 * @param invocation 语句表达式内部包含MethodInvocation
	 * [ ClassName . ] super .
         [ < Type { , Type } > ]
         Identifier ( [ Expression { , Expression } ]
	 * @return
	 */
	@Override
	public boolean visit(SuperMethodInvocation invocation) {
		metric +=MetricCognitiveComplexity.SuperMethod; 
		String imn = invocation.getName().getIdentifier(); //找出方法名。
		addInvoMethod(imn);//将来要从identiferNames排除此值。
		addInvoMethod("super");//super 会是visit(SimpleName simple)里的SimpleName吗？
		Name refName = 	invocation.getQualifier();
		if( refName!=null )
		{
			String strItem = 	refName.getFullyQualifiedName();
			String[] strParsed = strItem.split("\\.");
			for( String parsed : strParsed )
				addInvoMethod(parsed);
		}
		return true; //用true识别嵌套。
	}
	
	/**Lambda表达式，复杂度+3
	 * 此部分内容，实际可能很复杂，简化了问题。比如Lambda的参数，Lambda body内可能包含许多SimpleName
	 * @param lambda 语句表达式内部包含lambda
	 * @return
	 */
	@Override
	public boolean visit(LambdaExpression lambda) {
		metric +=MetricCognitiveComplexity.LambdalExp;
		//将该lambda表达式所有的参数都加到invoMethods中。
		lambda.accept(  new ASTVisitor() { 
						public boolean visit(VariableDeclarationFragment vds) { 
							String identifer = vds.getName().getIdentifier();
							addInvoMethod(identifer);
							return true;
						}//end of visit
					}//end of ASTVisitor
					); //end of accept.
		/*此处有bug ,将来要从identiferNames排除invoMethods时，可能出现问题。
		 *因为包含该lambda的语句与lambda可能包含相同标识符。
		 *一个还过得去的解决方案，设置标志，调用两次Accept，分开lamada与其它表达式的复杂度计算。
		 */
		
		return true; //用true识别嵌套。
	}
	
	/**CreationReference，复杂度+2 
	 * 格式：     Type ::  [ < Type { , Type } > ]    new
	 * @param reference  创建型方法引用 ClassName::new 
	 * @return
	 */
	@Override
	public boolean visit(CreationReference reference) {
		metric +=MetricCognitiveComplexity.RefCreation;
		//将该引用的type名都加到invoMethods中。
		reference.accept(  new ASTVisitor() { 
						public boolean visit(SimpleName sn) { 
							String identifer = sn.getIdentifier();
							addInvoMethod(identifer);
							return false;
						}//end of visit
					}//end of ASTVisitor
					); //end of accept.
		//可能bug: 该引用的创建TYPE可能未加入。
		return false; //简化问题，不识别嵌套。
	}
	
	/**ExpressionMethodReference，复杂度+3 
	 * 格式：     Expression :: [ < Type { , Type } > ]      Identifier
	 * @param reference  引用静态方法 和静态方法调用相比，只是把.换为::
	 * @return
	 */
	@Override
	public boolean visit(ExpressionMethodReference reference) {
		metric +=MetricCognitiveComplexity.RefExMethod;
		//将该引用的type名都加到invoMethods中。
		reference.accept(  new ASTVisitor() { 
						public boolean visit(SimpleName sn) { 
							String identifer = sn.getIdentifier();
							addInvoMethod(identifer);
							return false;//没有检索子节点。
						}//end of visit
					}//end of ASTVisitor
					); //end of accept.
		//bug: 该引用的Express未加入。
		String identifer = reference.getName().getIdentifier();
		addInvoMethod(identifer);
		return false; //没有检索子节点。
	}
	
	
	/**SuperMethodReference，复杂度+3 
	 * 格式：     [ ClassName . ] super ::   [ < Type { , Type } > ]       Identifier
	 * @param reference 
	 * @return
	 */
	@Override
	public boolean visit(SuperMethodReference reference) {
		metric +=MetricCognitiveComplexity.RefSupMethod;
		//将该引用的type名都加到invoMethods中。
		reference.accept(  new ASTVisitor() { 
						public boolean visit(SimpleName sn) { 
							String identifer =sn.getIdentifier();
							addInvoMethod(identifer);
							return false;//没有检索子节点。
						}//end of visit
					}//end of ASTVisitor
					); //end of accept.		
		Name refName = 	reference.getQualifier();
		if( refName!=null )
		{
			String strItem = 	refName.getFullyQualifiedName();
			String[] strParsed = strItem.split("\\.");
			for( String parsed : strParsed )
				addInvoMethod(parsed);
		}
		String identifer = reference.getName().getIdentifier();
		addInvoMethod(identifer);
		addInvoMethod("super");
		return false;//没有检索子节点。
	}
	
	/**TypeMethodReference，复杂度+3 
	 * 格式：  Type ::    [ < Type { , Type } > ]         Identifier
	 * @param reference  
	 * @return
	 */
	@Override
	public boolean visit( TypeMethodReference reference) {
		metric +=MetricCognitiveComplexity.RefTypMethod;
		//将该引用的type名都加到invoMethods中。
		reference.accept(  new ASTVisitor() { 
						public boolean visit(SimpleName sn) { 
							String identifer = sn.getIdentifier();
							addInvoMethod(identifer);
							return false;//没有检索子节点。
						}//end of visit
					}//end of ASTVisitor
					); //end of accept.	
		String identifer = reference.getName().getIdentifier();
		addInvoMethod(identifer);
		return false;//没有检索子节点。
	}

	/**  Conditional expression AST node type.  
	 * 格式： Expression ? Expression : Expression   三元表达式，复杂度 +3
	 */
	@Override
	public boolean visit(ConditionalExpression ceExpress) {
		metric +=MetricCognitiveComplexity.ConditionExpress; 
		return true; //识别嵌套。
	}
		
	/**记录该语句的所有标识符。排除保留字、函数名、类名、方法名。
	 * @param  simple 找出语句的所有SimpleName，将来计算他们的复杂度。
	 * 局部变量，复杂度+1
	 * 参数，复杂度+2
	 * 类属性等，复杂度+3
	 * @return
	 */
	@Override
	public boolean visit(SimpleName simple) {
		addIdentiferName(simple.getIdentifier());
		return false; //没有子节点。
	}
	
	/**条件表达式中有&& || != 三种类型，每多一种类型复杂度+1，
	 * @param invocation 语句表达式内部包含MethodInvocation
	 * @return
	 */
	@Override
	public boolean visit(InfixExpression infExpress) {
		addInfixOperator(infExpress.getOperator());
		return true; //识别嵌套。age>0 && df>10 || df<20 里面有5个关系表达式符号，必须true
	}
	
	//前缀表达式 ++ --
	@Override
	public boolean visit(PrefixExpression prefExpress) {
		addPrefPostOperator(prefExpress.getOperator());
		return true; //识别嵌套。
	}

	//后缀表达式  ++ -- ! + - % 
	@Override
	public boolean visit(PostfixExpression postExpress) {
		addPrefPostOperator(postExpress.getOperator());
		return true; //识别嵌套。
	}

	//当次计算出的认知复杂度
	public int getMetric() {
		return metric;
	}
	
	//记录该语句的所有标识符。排除保留字、函数名、类名、方法名。
	public List<String> getIdentiferNames() {
		return identiferNames;
	}
	
	//记录函数名、类名、方法名
	public List<String> getInvoMethods() {
		return invoMethods;
	}
	
	//检查是否新的条件操作符，未出现过的添加入其中。
	private void addInfixOperator(InfixExpression.Operator ifOpor)
	{
		String strOpor ="";
		if( ifOpor==InfixExpression.Operator.CONDITIONAL_AND )
			strOpor = "&&";
		else if( ifOpor==InfixExpression.Operator.CONDITIONAL_OR )
			strOpor = "||";
		else if( ifOpor==InfixExpression.Operator.XOR )
			strOpor = "^";
		else if( ifOpor==InfixExpression.Operator.AND )
			strOpor = "&";
		else if( ifOpor==InfixExpression.Operator.OR )
			strOpor = "|";
		else
			return; //其它类型不增加复杂度。
		if( ifOpor==InfixExpression.Operator.CONDITIONAL_AND
				|| ifOpor==InfixExpression.Operator.CONDITIONAL_OR )
		{
			if( !isExistOperator(logicOperators,strOpor) )
				logicOperators.add(strOpor);
		}
		else
		{
			if( !isExistOperator(strayOperators,strOpor) )
				strayOperators.add(strOpor);
		}
	}
	
	//检查是否新的前缀表达式操作符，未出现过的添加入其中。
	private void addPrefPostOperator(PrefixExpression.Operator ifOpor)
	{
		String strOpor ="";
		if( ifOpor==PrefixExpression.Operator.DECREMENT )
			strOpor = "++";
		else if( ifOpor==PrefixExpression.Operator.INCREMENT )
			strOpor = "--";
		else if( ifOpor==PrefixExpression.Operator.NOT )
			strOpor = "!";
		else if( ifOpor==PrefixExpression.Operator.COMPLEMENT )
			strOpor = "~";
		else
			return; //其它类型不增加复杂度。
		if( ifOpor==PrefixExpression.Operator.NOT )
		{
			if( !isExistOperator(logicOperators,strOpor) )
				logicOperators.add(strOpor);
		}
		else
		{
			if( !isExistOperator(strayOperators,strOpor) )
				strayOperators.add(strOpor);
		}
	}
	
	//检查是否新的后缀表达式操作符，未出现过的添加入其中。
	private void addPrefPostOperator(PostfixExpression.Operator ifOpor)
	{
		String strOpor ="";
		if( ifOpor==PostfixExpression.Operator.DECREMENT )
			strOpor = "++";
		else if( ifOpor==PostfixExpression.Operator.INCREMENT )
			strOpor = "--";
		else
			return; //其它类型不增加复杂度。实际上后缀表达式操作符只有++ --
		if( !isExistOperator(strayOperators,strOpor) )
			strayOperators.add(strOpor);
	}
	
	//opList中存在opor吗？true=存在；false:不存在。
	private boolean isExistOperator(List<String> opList,String opor)
	{
		boolean found = false;
		for( String item: opList )
		{
			if( item.contentEquals(opor) )
			{
				found = true;
				break;
			}
		}
		return found;
	}

	/** identifer 加到identiferNames中，将来用于计算复杂度。
	 * identiferNames 记录该语句的所有标识符。排除保留字、函数名、类名、方法名。
	 * @param identifer
	 */
	private void addIdentiferName(String identifer)
	{
		if( !isExistOperator(identiferNames,identifer) )
			identiferNames.add(identifer);
	}

	/** invoname 加到invoMethods中，将来用于计算复杂度。
	 * invoMethods记录函数名、类名、方法名，将来要从identiferNames中删除它们。
	 * @param identifer
	 */
	private void addInvoMethod(String invoname)
	{
		if( !isExistOperator(invoMethods,invoname) )
			invoMethods.add(invoname);
	}
	
	/*identiferNames中排除在invoMethods中的符号。
	 * 因为identiferNames是语句的所有标识符，而invoMethods是包含类名、方法名等方法调用的标识符。
	 * 在后续计算复杂度的步骤中，只计算类属性、参数、局部变量的影响；显然，不能再考虑invoMethods内标识符
	 */
	public void excludeInvoMethodFromIdentiferName()
	{
		Iterator<String> iterator = identiferNames.iterator();
        while (iterator.hasNext()) 
		{
        	String identifer = iterator.next();
        	if( isExistOperator(invoMethods,identifer) )
        		iterator.remove();
		}//end of while...
	}
}
