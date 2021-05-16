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
	private String name;  //��������
	/*
	 * ע�⣺ localVariables�������ڲ��ࡢǶ���ࡢ�������inner class�ľֲ�������
	 * ���������ȼ����⣬Ҳ����ʵ������
	 */
	private List<String> localVariables;    // �÷��������оֲ�������
	private List<String> parameters;    // �÷����Ĳ������б�
	/*
	 * type �������ͣ�1=���캯����2=getter or setter, 3=Java/C++��ͨ��ֱ�ӷ�����C�ĺ�������
	 *               4=Initializer, ��ĳ�ʼ���飬���⴦���䷽����Ϊclass_initializer��
	 *               5,����Ƕ����ķ���������CK�ȸ��Ӷ�ʱ���⴦��
	 *               6����Ĵ���ֵ������������䡣 class_FieldDeclaration
	 *               11,C++�ļ��Ķ��㺯����
	 *               12��ȱ��ͷ�ļ�����ķ�����������C++�ļ��Ķ��㺯��(Ҳ������ͷ�ļ�������ͷ�ļ������ļ�����)
	 *               13,C++ȫ�ֱ������ļ��������������䣬TopDeclartionCpp��ķ�����
	 *               21,C����ı�������������һ�������С�
	 */
	private int type; // ��������
	private List<StatementContext> statements; //�÷�����������䡣
	//���¶�������ķ�������������
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

	/** ������������䵽�˷����С�
	 * @param statements
	 */
	public void fillStatementsToMethod(List<StatementContext> sContexts)
	{
		for( StatementContext cc : sContexts )
			statements.add(cc);
	}
	
	/** Ϊ��λ��Ĵ���ֵ������������䣬�ڷ���������б�����ӵ�������䡣
	 * @param stmtContext
	 */
	public void addOneStatement(StatementContext stmtContext)
	{
		statements.add(stmtContext);
	}
	
	//��������
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	 // �÷��������оֲ�������
	public List<String> getLocalVariables() {
		return localVariables;
	}

	public void setLocalVariables(List<String> localVariables) {
		this.localVariables = localVariables;
	}

	//������ע����setLocalVariables�Ĳ��
	public void copyLocalVariables(List<String> varLst) {
		for( String var : varLst )
			localVariables.add(var);
	}

	// �÷����Ĳ������б�
	public List<String> getParameters() {
		return parameters;
	}

	public void setParameters(List<String> parameters) {
		this.parameters = parameters;
	}

	//���һ�������������б�
	public void addParameter(String para)
	{
		parameters.add(para);
	}
	
	//�������ͣ�1=���캯����2=getter or setter, 3=��ͨ������
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	/** ����Ϊ���ⷽ����
	 * �÷��������ⷽ����ֻ����C�����ռ����к����������䡣
	 */
	public void setGlobalVariableOfC()
	{
		name = GlobalVariableOfC;
		type = 21;
	}
	
	/** ����Ϊ���ⷽ����TopDeclartionCpp��ķ�����
	 * �÷��������ⷽ����ֻ����C++�����ռ�����C++ȫ�ֱ������ļ��������������䡣
	 */
	public void setGlobalVariableOfCpp()
	{
		name = GlobalVariableOfCpp;
		type = 13;
	}
	
	//�÷��������ⷽ���𣿣�ֻ����C�����ռ����к����������䡣��
	public boolean isGlobalVariableOfC()
	{
		if( type==21 )
			return true;
		else
			return false;
	}
	
	//�÷��������ⷽ���𣿣�C++�����ռ�����C++ȫ�ֱ������ļ��������������䣩
	public boolean isGlobalVariableOfCpp()
	{
		if( type==13 )
			return true;
		else
			return false;
	}
		
	//���ⷽ�������������ڲ�����ڲ���ķ�����
	public void setInner2Type()
	{
		this.type = 5;
	}
	
	//�÷�����������䡣
	public List<StatementContext> getStatements() {
		return statements;
	}
	
	//�÷��������������
	public int getNumberOfStatements()
	{
		return statements.size();
	}
	
	//��һ��鷽���ڵ���䣬���ʹ���˾ֲ�������ÿʹ��һ������ͬ�������ʹ�ã���Ϊһ���������Ӹ��Ӷ�1.
	private void adjustComplexMetricWithVariable()
	{
		for( String variab : localVariables )
		{
			for( StatementContext sc : statements )
				sc.adjustComplexMetricWithVariable(variab);
		}
	}
	
	//��һ��鷽���ڵ���䣬���ʹ���˾ֲ�������ÿʹ��һ������ͬ�������ʹ�ã���Ϊһ���������Ӹ��Ӷ�2.
	private void adjustComplexMetricWithParameter()
	{
		for( String para : parameters )
		{
			for( StatementContext sc : statements )
				sc.adjustComplexMetricWithParameter(para);
		}
	}
	
	//��һ��鷽���ڵ���䣬���ʹ���˾ֲ������Ͳ�������Ӧ�����Ӹ��Ӷ�.
	public void adjustComplexMetricWithParameterAndVaiable()
	{
		adjustComplexMetricWithVariable();
		adjustComplexMetricWithParameter();
	}
	
	//��һ��鷽���ڵ���䣬���ʹ����������ԣ���Ӧ�����Ӹ��Ӷ�.
	public void adjustComplexMetricWithAttribute(List<String> attributes)
	{
		//type=6����Ĵ���ֵ������������䡣 class_FieldDeclaration
		//���ﲻ�ж������Ƿ�Ϊ6��ȱ����ÿ������ֵ���������������Ȼ����֪���Ӷ�2��
		for( String attr : attributes )
		{
			for( StatementContext sc : statements )
				sc.adjustComplexMetricWithAttribute(attr);
		}
	}
	
	//��һ��鷽���ڵ���䣬���ʹ���˺궨�壬��Ӧ�����Ӹ��Ӷ�.
	public void adjustComplexMetricWithMacroDefinition(List<String> macroDefines)
	{
		for( String mdef : macroDefines )
		{
			for( StatementContext sc : statements )
				sc.adjustComplexMetricWithMacroDefinition(mdef);
		}
	}
	
	/**���кţ��ҳ���Ӧ����֪���Ӷ�
	 * @param lineno  �к�
	 * @return �к�δ�ҵ����򷵻�-1. lineno�϶����ڸ÷����У�����-2.
	 */
	public int getCognitiveComplexByLineno(int lineno)
	{
		int cognitive = -2;
		int startLineno = this.getStartLine();
		int endLineno = this.getEndLine();
		if( endLineno>startLineno )
		{ //endLineno��startLineno����ʼ��Ϊ0��type=6,13,21��Щ���ͣ����޷�����ʼ�кͽ����С�
			if( lineno<startLineno || lineno>endLineno )
				return cognitive;
		}
		cognitive = -1;
		List<StatementContext> finds=new ArrayList<>(); //�����к�lineno������StatementContext
		for( StatementContext stmtCtx : statements )
		{
			int startLine = stmtCtx.getStartLine();
			int endLine = stmtCtx.getEndLine();
			if( (lineno>=startLine) && (lineno<=endLine) )
				finds.add(stmtCtx);
		}
		/*Ϊ�������������Ͽ�ִ�����д��ͬһ�С�
		 * �磺 (49,51)=6  (51,51)=4 (51,51)=7  lineno = 49��ȡ6; =50 ȡ6; =51ȡ7;
		 * ����ԭ����startLine-lineno��Сֵ����䣬������������ж�����ȡ�������ֵ��Ϊ���Ӷ�ֵ��
		 */
		int number = finds.size();
		if( number<=0 ) //δ�ҵ���
		{}
		else if ( number==1 )
		{//ֻ��һ��StatementContext
			cognitive = finds.get(0).getCognitiveComplexity();
		}
		else 
		{//�ж������ôֻ����ʼ�к�= lineno�Ĳŷ���Ҫ�󡣣���ִ����䲻�����໥Ƕ�ס���
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
	
	//��ʾ������Ϣ
	@Override
	public void showMe()
	{
		System.out.print("       Method:  "+name+", type=: "+type+"   parameter: ");
		//��ӡ�������б�
		for( String para : parameters )
			System.out.print(para+",  ");
		System.out.println(".  "+parameters.size());
		//��ӡ�ֲ��������б�
		System.out.print("                Variable: ");
		for( String var : localVariables )
			System.out.print(var+",  ");
		System.out.println("."+localVariables.size());
		super.showMe();
		//��ӡ����������ݡ�
		for( StatementContext sContext : statements )
			sContext.showMe();
	}
}
