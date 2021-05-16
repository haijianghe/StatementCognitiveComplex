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

/**  ����Ϊ��λ���㸴�Ӷȣ������Ǳ��ʽ���ͱ��������ڵ�Ƕ����䡣
 *  ������Ϊ��������visit���ҳ�������ڵĺ������á��߼���������ֲ������������������Եȡ�
 * @author Administrator
 *
 *Direct Known Subclasses:  ���ʽ���ࡣ
 * Annotation, ArrayAccess, ArrayCreation, ArrayInitializer, Assignment, BooleanLiteral, CastExpression, 
 * CharacterLiteral, ClassInstanceCreation, ConditionalExpression, FieldAccess, InfixExpression, 
 * InstanceofExpression, LambdaExpression, MethodInvocation, MethodReference, Name, NullLiteral, 
 * NumberLiteral, ParenthesizedExpression, PostfixExpression, PrefixExpression, StringLiteral, 
 * SuperFieldAccess, SuperMethodInvocation, ThisExpression, TypeLiteral, VariableDeclarationExpression

 */
public class JavaFragmentVisitor extends ASTVisitor{
	private int metric;  //���μ��������֪���Ӷ�
	List<String> identiferNames; //��¼���������б�ʶ�����ų������֡�����������������������
	List<String> invoMethods; //��¼��������������������������Ҫ��identiferNames��ɾ�����ǡ�
	List<String> logicOperators;  //�������������������ظ���&& || !
	List<String> strayOperators;  //���Infix,ǰ׺���ʽ����������׺���ʽ���������������ظ��� ++ -- ~ ^ & | 
	
	public JavaFragmentVisitor()
	{
		metric = 0;
		identiferNames = new ArrayList<>();
		invoMethods = new ArrayList<>();
		logicOperators = new ArrayList<>();
		strayOperators = new ArrayList<>();
	}
	
	/*  ������֪���Ӷȣ�������������������ǰ׺(��׺)���ʽ������������
	 * ������������������ÿ��+1
	 * ǰ׺(��׺)���ʽ������������,��������-1��Ϊ���Ӷȡ�
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
	/**���ڲ��ķ������ã����Ӷ�+2;     �ⲿ�ķ������ã����Ӷ�+3
	 * @param invocation �����ʽ�ڲ�����MethodInvocation
	 * @return
	 */
	@Override
	public boolean visit(MethodInvocation invocation) {
		Expression  express = invocation.getExpression();
		if( express==null )
		{
			metric +=MetricCognitiveComplexity.InnerMethod; //���ڲ��ķ������ã����Ӷ�+2
			String imn = invocation.getName().getIdentifier(); //�ҳ���������
			addInvoMethod(imn);//����Ҫ��identiferNames�ų���ֵ��
		}
		else
		{
			metric +=MetricCognitiveComplexity.ExternMethod; //�ⲿ�ķ������ã����Ӷ�+3
			if( express instanceof QualifiedName )
			{
				String strItem = ((QualifiedName)express).getFullyQualifiedName();
				String[] strParsed = strItem.split("\\.");
				for( String parsed : strParsed )
					addInvoMethod(parsed);
				String imn = invocation.getName().getIdentifier(); //�ҳ���������
				addInvoMethod(imn);
			}
			else if( express instanceof SimpleName )
			{
				String strItem = ((SimpleName)express).getIdentifier();
				addInvoMethod(strItem);//����Ҫ��identiferNames�ų���ֵ��
				String imn = invocation.getName().getIdentifier(); //�ҳ���������
				addInvoMethod(imn);
			}
			else
			{
				//TypeLiteral:��ʽ    ( Type | void ) . class
				if( express instanceof TypeLiteral )
					addInvoMethod("class");
				//һ��һ�����ҳ�express�����ͣ�̫�鷳���򵥵ؿ�����.���֡�
				String strItem = express.toString();
				String[] strParsed = strItem.split("\\.");
				for( String parsed : strParsed )
					addInvoMethod(parsed);
			}
		}
		return true; //��trueʶ��Ƕ�ס�
	}
	/**���෽���ĵ��ã����Ӷ�+2 ��Ϊ���������͵ĸ��Ӷȣ����ܵ�Ϊ3��
	 * @param invocation �����ʽ�ڲ�����MethodInvocation
	 * [ ClassName . ] super .
         [ < Type { , Type } > ]
         Identifier ( [ Expression { , Expression } ]
	 * @return
	 */
	@Override
	public boolean visit(SuperMethodInvocation invocation) {
		metric +=MetricCognitiveComplexity.SuperMethod; 
		String imn = invocation.getName().getIdentifier(); //�ҳ���������
		addInvoMethod(imn);//����Ҫ��identiferNames�ų���ֵ��
		addInvoMethod("super");//super ����visit(SimpleName simple)���SimpleName��
		Name refName = 	invocation.getQualifier();
		if( refName!=null )
		{
			String strItem = 	refName.getFullyQualifiedName();
			String[] strParsed = strItem.split("\\.");
			for( String parsed : strParsed )
				addInvoMethod(parsed);
		}
		return true; //��trueʶ��Ƕ�ס�
	}
	
	/**Lambda���ʽ�����Ӷ�+3
	 * �˲������ݣ�ʵ�ʿ��ܸܺ��ӣ��������⡣����Lambda�Ĳ�����Lambda body�ڿ��ܰ������SimpleName
	 * @param lambda �����ʽ�ڲ�����lambda
	 * @return
	 */
	@Override
	public boolean visit(LambdaExpression lambda) {
		metric +=MetricCognitiveComplexity.LambdalExp;
		//����lambda���ʽ���еĲ������ӵ�invoMethods�С�
		lambda.accept(  new ASTVisitor() { 
						public boolean visit(VariableDeclarationFragment vds) { 
							String identifer = vds.getName().getIdentifier();
							addInvoMethod(identifer);
							return true;
						}//end of visit
					}//end of ASTVisitor
					); //end of accept.
		/*�˴���bug ,����Ҫ��identiferNames�ų�invoMethodsʱ�����ܳ������⡣
		 *��Ϊ������lambda�������lambda���ܰ�����ͬ��ʶ����
		 *һ��������ȥ�Ľ�����������ñ�־����������Accept���ֿ�lamada���������ʽ�ĸ��Ӷȼ��㡣
		 */
		
		return true; //��trueʶ��Ƕ�ס�
	}
	
	/**CreationReference�����Ӷ�+2 
	 * ��ʽ��     Type ::  [ < Type { , Type } > ]    new
	 * @param reference  �����ͷ������� ClassName::new 
	 * @return
	 */
	@Override
	public boolean visit(CreationReference reference) {
		metric +=MetricCognitiveComplexity.RefCreation;
		//�������õ�type�����ӵ�invoMethods�С�
		reference.accept(  new ASTVisitor() { 
						public boolean visit(SimpleName sn) { 
							String identifer = sn.getIdentifier();
							addInvoMethod(identifer);
							return false;
						}//end of visit
					}//end of ASTVisitor
					); //end of accept.
		//����bug: �����õĴ���TYPE����δ���롣
		return false; //�����⣬��ʶ��Ƕ�ס�
	}
	
	/**ExpressionMethodReference�����Ӷ�+3 
	 * ��ʽ��     Expression :: [ < Type { , Type } > ]      Identifier
	 * @param reference  ���þ�̬���� �;�̬����������ȣ�ֻ�ǰ�.��Ϊ::
	 * @return
	 */
	@Override
	public boolean visit(ExpressionMethodReference reference) {
		metric +=MetricCognitiveComplexity.RefExMethod;
		//�������õ�type�����ӵ�invoMethods�С�
		reference.accept(  new ASTVisitor() { 
						public boolean visit(SimpleName sn) { 
							String identifer = sn.getIdentifier();
							addInvoMethod(identifer);
							return false;//û�м����ӽڵ㡣
						}//end of visit
					}//end of ASTVisitor
					); //end of accept.
		//bug: �����õ�Expressδ���롣
		String identifer = reference.getName().getIdentifier();
		addInvoMethod(identifer);
		return false; //û�м����ӽڵ㡣
	}
	
	
	/**SuperMethodReference�����Ӷ�+3 
	 * ��ʽ��     [ ClassName . ] super ::   [ < Type { , Type } > ]       Identifier
	 * @param reference 
	 * @return
	 */
	@Override
	public boolean visit(SuperMethodReference reference) {
		metric +=MetricCognitiveComplexity.RefSupMethod;
		//�������õ�type�����ӵ�invoMethods�С�
		reference.accept(  new ASTVisitor() { 
						public boolean visit(SimpleName sn) { 
							String identifer =sn.getIdentifier();
							addInvoMethod(identifer);
							return false;//û�м����ӽڵ㡣
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
		return false;//û�м����ӽڵ㡣
	}
	
	/**TypeMethodReference�����Ӷ�+3 
	 * ��ʽ��  Type ::    [ < Type { , Type } > ]         Identifier
	 * @param reference  
	 * @return
	 */
	@Override
	public boolean visit( TypeMethodReference reference) {
		metric +=MetricCognitiveComplexity.RefTypMethod;
		//�������õ�type�����ӵ�invoMethods�С�
		reference.accept(  new ASTVisitor() { 
						public boolean visit(SimpleName sn) { 
							String identifer = sn.getIdentifier();
							addInvoMethod(identifer);
							return false;//û�м����ӽڵ㡣
						}//end of visit
					}//end of ASTVisitor
					); //end of accept.	
		String identifer = reference.getName().getIdentifier();
		addInvoMethod(identifer);
		return false;//û�м����ӽڵ㡣
	}

	/**  Conditional expression AST node type.  
	 * ��ʽ�� Expression ? Expression : Expression   ��Ԫ���ʽ�����Ӷ� +3
	 */
	@Override
	public boolean visit(ConditionalExpression ceExpress) {
		metric +=MetricCognitiveComplexity.ConditionExpress; 
		return true; //ʶ��Ƕ�ס�
	}
		
	/**��¼���������б�ʶ�����ų������֡�����������������������
	 * @param  simple �ҳ���������SimpleName�������������ǵĸ��Ӷȡ�
	 * �ֲ����������Ӷ�+1
	 * ���������Ӷ�+2
	 * �����Եȣ����Ӷ�+3
	 * @return
	 */
	@Override
	public boolean visit(SimpleName simple) {
		addIdentiferName(simple.getIdentifier());
		return false; //û���ӽڵ㡣
	}
	
	/**�������ʽ����&& || != �������ͣ�ÿ��һ�����͸��Ӷ�+1��
	 * @param invocation �����ʽ�ڲ�����MethodInvocation
	 * @return
	 */
	@Override
	public boolean visit(InfixExpression infExpress) {
		addInfixOperator(infExpress.getOperator());
		return true; //ʶ��Ƕ�ס�age>0 && df>10 || df<20 ������5����ϵ���ʽ���ţ�����true
	}
	
	//ǰ׺���ʽ ++ --
	@Override
	public boolean visit(PrefixExpression prefExpress) {
		addPrefPostOperator(prefExpress.getOperator());
		return true; //ʶ��Ƕ�ס�
	}

	//��׺���ʽ  ++ -- ! + - % 
	@Override
	public boolean visit(PostfixExpression postExpress) {
		addPrefPostOperator(postExpress.getOperator());
		return true; //ʶ��Ƕ�ס�
	}

	//���μ��������֪���Ӷ�
	public int getMetric() {
		return metric;
	}
	
	//��¼���������б�ʶ�����ų������֡�����������������������
	public List<String> getIdentiferNames() {
		return identiferNames;
	}
	
	//��¼��������������������
	public List<String> getInvoMethods() {
		return invoMethods;
	}
	
	//����Ƿ��µ�������������δ���ֹ�����������С�
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
			return; //�������Ͳ����Ӹ��Ӷȡ�
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
	
	//����Ƿ��µ�ǰ׺���ʽ��������δ���ֹ�����������С�
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
			return; //�������Ͳ����Ӹ��Ӷȡ�
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
	
	//����Ƿ��µĺ�׺���ʽ��������δ���ֹ�����������С�
	private void addPrefPostOperator(PostfixExpression.Operator ifOpor)
	{
		String strOpor ="";
		if( ifOpor==PostfixExpression.Operator.DECREMENT )
			strOpor = "++";
		else if( ifOpor==PostfixExpression.Operator.INCREMENT )
			strOpor = "--";
		else
			return; //�������Ͳ����Ӹ��Ӷȡ�ʵ���Ϻ�׺���ʽ������ֻ��++ --
		if( !isExistOperator(strayOperators,strOpor) )
			strayOperators.add(strOpor);
	}
	
	//opList�д���opor��true=���ڣ�false:�����ڡ�
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

	/** identifer �ӵ�identiferNames�У��������ڼ��㸴�Ӷȡ�
	 * identiferNames ��¼���������б�ʶ�����ų������֡�����������������������
	 * @param identifer
	 */
	private void addIdentiferName(String identifer)
	{
		if( !isExistOperator(identiferNames,identifer) )
			identiferNames.add(identifer);
	}

	/** invoname �ӵ�invoMethods�У��������ڼ��㸴�Ӷȡ�
	 * invoMethods��¼��������������������������Ҫ��identiferNames��ɾ�����ǡ�
	 * @param identifer
	 */
	private void addInvoMethod(String invoname)
	{
		if( !isExistOperator(invoMethods,invoname) )
			invoMethods.add(invoname);
	}
	
	/*identiferNames���ų���invoMethods�еķ��š�
	 * ��ΪidentiferNames���������б�ʶ������invoMethods�ǰ����������������ȷ������õı�ʶ����
	 * �ں������㸴�ӶȵĲ����У�ֻ���������ԡ��������ֲ�������Ӱ�죻��Ȼ�������ٿ���invoMethods�ڱ�ʶ��
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
