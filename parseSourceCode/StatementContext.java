/**
 * 
 */
package parseSourceCode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Administrator
 *
 */
public class StatementContext extends AbstractSourceContext{
	/*
	 * 计算宏定义、内部变量、参数、属性、全局变量对复杂度的影响时，
	 * 若AccumulationComplex=false; 则每条语句针对同类型的情况，只计算一次，
	 * 比如，使用了多个参数，只增加一次参数的复杂度。
	 */
	private static boolean AccumulationComplex=true; 
	private boolean bLocalVaiable;  //此语句是否曾经加过局部变量的复杂度值 
	private boolean bMacroDefine;   //此语句是否曾经加过宏定义的复杂度值 
	private boolean bParameter;     //此语句是否曾经加过参数的复杂度值 
	private boolean bAttribute;     //此语句是否曾经加过属性、全局变量的复杂度值 
	
	private int cognitiveComplexity; //认知复杂度，以行为单位。
	/*方法内局部变量，每一个，增加认知复杂度1
	 * 方法的参数变量，每一个，增加认知复杂度2
	 * 类的属性或方法的外部变量，每一个，增加认知复杂度3
	 * */
	List<String> simpleNames; //记录该语句的所有标识符。排除保留字、函数名、类名、方法名。
	/*
	 *属性， 局部变量，参数变量，计算结束后，会从simpleNames删除。
	 *所以，一般情况下，simpleNames为空。
	 *但是，在for，while等语句里面，可以定义内部变量，这些变量不能影响复杂度，最后会留在simpleNames里。
	 *还有，如果局部变量的数据类型并非原子型，在调用它的方法时，会忽略变量名的认知复杂度，并且变量名会留在simpleNames里。
	 *     如： 有局部变量SimpleSample ssm;   ssm.get();
	 *     get()调用有复杂度，而ssm是局部变量有复杂度；
	 *     我懒得修改代码，最后的结果，ssm并不计算复杂度，且其名会留在simpleNames里。
	 */
	
	public StatementContext()
	{
		super.setEndLine(0);
		super.setStartLine(0);
		cognitiveComplexity = 0;
		simpleNames = new ArrayList<>();
		bLocalVaiable = false;
		bMacroDefine = false;
		bParameter = false;
		bAttribute = false;
	}
	
	//认知复杂度，以行为单位。
	public int getCognitiveComplexity() {
		return cognitiveComplexity;
	}


	public void setCognitiveComplexity(int cognitiveComplexity) {
		this.cognitiveComplexity = cognitiveComplexity;
	}

	//记录该语句的所有标识符。排除保留字、函数名、类名、方法名。
	public List<String> getSimpleNames() {
		return simpleNames;
	}


	public void setSimpleNames(List<String> simpleNames) {
		this.simpleNames = simpleNames;
	}

	/** 为for,while,do while等特殊语句加复杂度。
	 * @param complexMetric
	 */
	public void addSpeccialComplex(int complexMetric)
	{
		cognitiveComplexity += complexMetric;
	}
	
	/** for语句里 for单独一行，while语句里 while单独一行 
	 * 这种情况下，for(while,if, foreach,....)所在的行号不是整个语句的起始行号，调用此方法后，可解决整个问题。
	 * @param lineno
	 */
	public void enlargeStartLineno(int lineno)
	{
		if( getStartLine()>lineno )
			setStartLine(lineno);
	}
	
	/**  simpleNames是否存在某个表示符（变量名，函数名，...）
	 * @param identifer
	 * @return true: 存在；
	 */
	private boolean isExistSimpleNames(String identifer)
	{
		boolean result = false;
		for( String item: simpleNames )
		{
			if( item.contentEquals(identifer) )
			{
				result = true;
				break;
			}
		}
		return result;
	}
	/** 这是一条复杂的语句，包含多个部分，将语句扩展，以便加入新的内容sContext
	 * 扩展内容包括： 开始行扩展，结束行扩展，认知复杂度累加，simpleNames累加。
	 * @param sContext 是一条语句的一部分，加入进来
	 */
	public void enlargeToComplexStatement(StatementContext sContext)
	{
		//起始行，结束行扩展
		int scstart = sContext.getStartLine();
		int scend = sContext.getEndLine();
		if( getStartLine()==0 || getEndLine()==0 )
		{ //都等于0，说明该语句还未读AST。
			setStartLine(scstart);
			setEndLine(scend);
			cognitiveComplexity = sContext.getCognitiveComplexity();
			simpleNames = sContext.getSimpleNames();
		}
		else
		{
			//起始行扩展，找小的行号
			if( getStartLine()>scstart )
				setStartLine(scstart);
			//结束行扩展，找大的行号
			if( getEndLine()<scend )
				setEndLine(scend);
			//认知复杂度累加
			cognitiveComplexity += sContext.getCognitiveComplexity();
			//simpleNames累加，并不重复。 
			List<String> simpleOfSC = sContext.getSimpleNames();
			for( String item : simpleOfSC)
			{
				if( !isExistSimpleNames(item) )
					simpleNames.add(item);
			}
		}//end of else
	}
	
	//将制定的宏定义，加入语句的simpleNames；将来添加复杂度。
	public void addMacroDefinitionName(String macroName)
	{
		if( !isExistSimpleNames(macroName) )
			simpleNames.add(macroName);
	}
	
	//逐一检查方法内的语句，如果使用了局部变量，每使用一个（相同变量多次使用，视为一个），增加复杂度1.
	//methodVariable 方法内的局部变量。
	public void adjustComplexMetricWithVariable(String methodVariable)
	{
		//两种情况才累加：允许累加，以前从没有加过局部变量的复杂度值. 
		if( (AccumulationComplex==false) && (bLocalVaiable==true) ) 
			return;
		for( Iterator<String> iterator = simpleNames.iterator(); iterator.hasNext();)
		{
			String snames = iterator.next();
			if( snames.contentEquals(methodVariable) )
			{
				cognitiveComplexity += MetricCognitiveComplexity.LocalVariable;
				iterator.remove();
				bLocalVaiable = true;//此语句加过局部变量的复杂度值 
				break;
			}//end of if
		}//end of for...
		
	}
	
	//逐一检查方法内的语句，如果使用了参数，每使用一个（相同参数多次使用，视为一个），增加复杂度2.
	//methodPara 方法的参数。
	public void adjustComplexMetricWithParameter(String methodPara)
	{
		//两种情况才累加：允许累加，以前从没有加过参数的复杂度值. 
		if( (AccumulationComplex==false) && (bParameter==true) ) 
			return;
		for( Iterator<String> iterator = simpleNames.iterator(); iterator.hasNext();)
		{
			String snames = iterator.next();
			if( snames.contentEquals(methodPara) )
			{
				cognitiveComplexity += MetricCognitiveComplexity.MethodParameter;
				iterator.remove();
				bParameter=true;
				break;
			}//end of if
		}//end of for...
	}
	
	/*逐一检查方法内的语句，如果使用了类的属性，每使用一个（相同属性多次使用，视为一个），增加复杂度3.
	 *classAttr 方法的参数。
	 *注意： 局部变量优先级最高，必须先调用adjustComplexMetricWithVariable
	 *       参数次之，  接着调用adjustComplexMetricWithParameter
	 *       类的属性再次之，最后才能调用adjustComplexMetricWithAttribute
	 */
	public void adjustComplexMetricWithAttribute(String classAttr)
	{
		//两种情况才累加：允许累加，以前从没有加过全局变量、属性的复杂度值. 
		if( (AccumulationComplex==false) && (bAttribute==true) ) 
			return;

		for( Iterator<String> iterator = simpleNames.iterator(); iterator.hasNext();)
		{
			String snames = iterator.next();
			if( snames.contentEquals(classAttr) )
			{
				cognitiveComplexity += MetricCognitiveComplexity.AttributeExtern;
				iterator.remove();
				bAttribute=true;
				break;
			}//end of if
		}//end of for...
	}
	
	/*逐一检查方法内的语句，如果使用了宏定义，每使用一个（相同属性多次使用，视为一个），增加复杂度2.
	 *macroDefine 宏定义名。
	 * 不区分对象形式的宏定义PASTObjectMacro和函数形式的宏定义PASTFunctionMacro,
	 */
	public void adjustComplexMetricWithMacroDefinition(String macroDefine)
	{
		//两种情况才累加：允许累加，以前从没有加过宏定义的复杂度值. 
		if( (AccumulationComplex==false) && (bMacroDefine==true) ) 
			return;

		for( Iterator<String> iterator = simpleNames.iterator(); iterator.hasNext();)
		{
			String snames = iterator.next();
			if( snames.contentEquals(macroDefine) )
			{
				cognitiveComplexity += MetricCognitiveComplexity.CCPPMacroDefinition;
				iterator.remove();
				bMacroDefine=true;
				break;
			}//end of if
		}//end of for...
	}
	
	//显示所有信息
	@Override
	public void showMe()
	{
		System.out.print("        ("+getStartLine()+","+getEndLine()+")="+cognitiveComplexity);
		System.out.print("        ");
		//打印simpleNames列表
		for( String para : simpleNames )
			System.out.print(para+",  ");
		System.out.println("=> "+simpleNames.size());
	}
}
