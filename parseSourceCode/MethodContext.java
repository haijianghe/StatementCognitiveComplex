/**
 * 
 */
package parseSourceCode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 *
 */
public class MethodContext extends AbstractSourceContext {
	private String name;  //方法名。
	/*
	 * 注意： localVariables将列入内部类、嵌套类、匿名类等inner class的局部变量。
	 * 这样做，既简化问题，也符合实际需求。
	 */
	private List<String> localVariables;    // 该方法的所有局部变量。
	private List<String> parameters;    // 该方法的参数名列表。
	/*
	 * type 方法类型，1=构造函数，2=getter or setter, 3=Java/C++普通的直接方法（C的函数）。
	 *               4=Initializer, 类的初始化块，特殊处理，其方法名为class_initializer；
	 *               5,二级嵌套类的方法，计算CK等复杂度时特殊处理，
	 *               6，类的带赋值的属性声明语句。 class_FieldDeclaration
	 *               11,C++文件的顶层函数；
	 *               12，缺少头文件的类的方法，类似于C++文件的顶层函数(也可能有头文件，不过头文件、主文件分离)
	 *               13,C++全局变量，文件顶层变量声明语句，TopDeclartionCpp类的方法。
	 *               21,C程序的变量声明语句放在一个函数中。
	 */
	private int type; // 方法类型
	private List<StatementContext> statements; //该方法的所有语句。
	//以下定义特殊的方法名或函数名。
	public final static String GlobalVariableOfC = "GlobalVariableOfC";// C: type=21
	public final static String GlobalVariableOfCpp = "GlobalVariableOfCpp";//C++: type=13
	
	public MethodContext()
	{
		super.setEndLine(0);
		super.setStartLine(0);
		name = "";
		localVariables = new ArrayList<>();
		parameters = new ArrayList<>();
		type = -1;
		statements = new ArrayList<>();		
	}

	/** 将语句的数据填充到此方法中。
	 * @param statements
	 */
	public void fillStatementsToMethod(List<StatementContext> sContexts)
	{
		for( StatementContext cc : sContexts )
			statements.add(cc);
	}
	
	/** 为定位类的带赋值的属性声明语句，在方法的语句列表中添加单个的语句。
	 * @param stmtContext
	 */
	public void addOneStatement(StatementContext stmtContext)
	{
		statements.add(stmtContext);
	}
	
	//方法名。
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	 // 该方法的所有局部变量。
	public List<String> getLocalVariables() {
		return localVariables;
	}

	public void setLocalVariables(List<String> localVariables) {
		this.localVariables = localVariables;
	}

	//拷贝，注意与setLocalVariables的差别
	public void copyLocalVariables(List<String> varLst) {
		for( String var : varLst )
			localVariables.add(var);
	}

	// 该方法的参数名列表。
	public List<String> getParameters() {
		return parameters;
	}

	public void setParameters(List<String> parameters) {
		this.parameters = parameters;
	}

	//添加一个参数到参数列表。
	public void addParameter(String para)
	{
		parameters.add(para);
	}
	
	//方法类型，1=构造函数，2=getter or setter, 3=普通方法。
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	/** 设置为特殊方法。
	 * 该方法是特殊方法，只属于C程序，收集所有函数体外的语句。
	 */
	public void setGlobalVariableOfC()
	{
		name = GlobalVariableOfC;
		type = 21;
	}
	
	/** 设置为特殊方法，TopDeclartionCpp类的方法。
	 * 该方法是特殊方法，只属于C++程序，收集所有C++全局变量，文件顶层变量声明语句。
	 */
	public void setGlobalVariableOfCpp()
	{
		name = GlobalVariableOfCpp;
		type = 13;
	}
	
	//该方法是特殊方法吗？（只属于C程序，收集所有函数体外的语句。）
	public boolean isGlobalVariableOfC()
	{
		if( type==21 )
			return true;
		else
			return false;
	}
	
	//该方法是特殊方法吗？（C++程序，收集所有C++全局变量，文件顶层变量声明语句）
	public boolean isGlobalVariableOfCpp()
	{
		if( type==13 )
			return true;
		else
			return false;
	}
		
	//特殊方法，是最顶层类的内部类的内部类的方法。
	public void setInner2Type()
	{
		this.type = 5;
	}
	
	//该方法的所有语句。
	public List<StatementContext> getStatements() {
		return statements;
	}
	
	//该方法的语句条数。
	public int getNumberOfStatements()
	{
		return statements.size();
	}
	
	//逐一检查方法内的语句，如果使用了局部变量，每使用一个（相同变量多次使用，视为一个），增加复杂度1.
	private void adjustComplexMetricWithVariable()
	{
		for( String variab : localVariables )
		{
			for( StatementContext sc : statements )
				sc.adjustComplexMetricWithVariable(variab);
		}
	}
	
	//逐一检查方法内的语句，如果使用了局部变量，每使用一个（相同变量多次使用，视为一个），增加复杂度2.
	private void adjustComplexMetricWithParameter()
	{
		for( String para : parameters )
		{
			for( StatementContext sc : statements )
				sc.adjustComplexMetricWithParameter(para);
		}
	}
	
	//逐一检查方法内的语句，如果使用了局部变量和参数，相应地增加复杂度.
	public void adjustComplexMetricWithParameterAndVaiable()
	{
		adjustComplexMetricWithVariable();
		adjustComplexMetricWithParameter();
	}
	
	//逐一检查方法内的语句，如果使用了类的属性，相应地增加复杂度.
	public void adjustComplexMetricWithAttribute(List<String> attributes)
	{
		//type=6，类的带赋值的属性声明语句。 class_FieldDeclaration
		//这里不判断类型是否为6，缺点是每条带赋值的属性声明语句天然有认知复杂度2。
		for( String attr : attributes )
		{
			for( StatementContext sc : statements )
				sc.adjustComplexMetricWithAttribute(attr);
		}
	}
	
	//逐一检查方法内的语句，如果使用了宏定义，相应地增加复杂度.
	public void adjustComplexMetricWithMacroDefinition(List<String> macroDefines)
	{
		for( String mdef : macroDefines )
		{
			for( StatementContext sc : statements )
				sc.adjustComplexMetricWithMacroDefinition(mdef);
		}
	}
	
	/**由行号，找出对应的认知复杂度
	 * @param lineno  行号
	 * @return 行号未找到，则返回-1. lineno肯定不在该方法中，返回-2.
	 */
	public int getCognitiveComplexByLineno(int lineno)
	{
		int cognitive = -2;
		int startLineno = this.getStartLine();
		int endLineno = this.getEndLine();
		if( endLineno>startLineno )
		{ //endLineno和startLineno都初始化为0，type=6,13,21这些类型，都无法读起始行和结束行。
			if( lineno<startLineno || lineno>endLineno )
				return cognitive;
		}
		cognitive = -1;
		List<StatementContext> finds=new ArrayList<>(); //包含行号lineno的所有StatementContext
		for( StatementContext stmtCtx : statements )
		{
			int startLine = stmtCtx.getStartLine();
			int endLine = stmtCtx.getEndLine();
			if( (lineno>=startLine) && (lineno<=endLine) )
				finds.add(stmtCtx);
		}
		/*为避免两条或以上可执行语句写在同一行。
		 * 如： (49,51)=6  (51,51)=4 (51,51)=7  lineno = 49，取6; =50 取6; =51取7;
		 * 查找原则：找startLine-lineno最小值的语句，如果最近的语句有多条，取其中最大值作为复杂度值。
		 */
		int number = finds.size();
		if( number<=0 ) //未找到。
		{}
		else if ( number==1 )
		{//只有一个StatementContext
			cognitive = finds.get(0).getCognitiveComplexity();
		}
		else 
		{//有多个。那么只有起始行号= lineno的才符合要求。（可执行语句不可能相互嵌套。）
			int maxv = 0;
			for( StatementContext stCtx : finds )
			{
				int startLine = stCtx.getStartLine();
				if( lineno==startLine )
				{
					int cognv = stCtx.getCognitiveComplexity();
					if( cognv>maxv )
						maxv = cognv;
				}
			}
			cognitive = maxv;
		}
		return cognitive;
	}
	
	//显示所有信息
	@Override
	public void showMe()
	{
		System.out.print("       Method:  "+name+", type=: "+type+"   parameter: ");
		//打印参数名列表
		for( String para : parameters )
			System.out.print(para+",  ");
		System.out.println(".  "+parameters.size());
		//打印局部变量名列表
		System.out.print("                Variable: ");
		for( String var : localVariables )
			System.out.print(var+",  ");
		System.out.println("."+localVariables.size());
		super.showMe();
		//打印所有语句数据。
		for( StatementContext sContext : statements )
			sContext.showMe();
	}
}
