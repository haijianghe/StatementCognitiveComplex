/**
 * 
 */
package parseSourceCode;

import java.util.ArrayList;
import java.util.List;

/** �������һ��Ƕ���࣬�����Ὣ�κ�Ƕ��������Ժͷ������������С�
 * @author Administrator
 *
 */
/**
 * @author Administrator
 *
 */
public class ClassContext extends AbstractSourceContext {
	private String name;  //������ӿ�����
	 /*C���Ժ�C++��ͷ�ļ������ļ����ֿܷ���ProjectContext��String���ļ�����ָ���ļ�,��parsingFilename���ǽ���ʱ�����ĵ�һ���ļ�����
	 ��Java��˵���򲻴��ڴ����⡣ProjectContext��String���ļ�����parsingFilename��ͬһ����������ʱ���ࡣ*/
	String parsingFilename; //��������������ĸ��ļ�������Ŀ¼��Ϣ�������ݶ�Java,c���࣬��C++��һ�����ࡣ
	/*
	 * ע�⣺ attributes�����������ڲ��ࡢǶ���ࡢ�������inner class�����ԡ�
	 */
	private List<String> attributes;    // ������������б�
	private String parentName;    // ��nesting=true,��Ϊ�����Ƕ����������
	private boolean nesting; //�����Ƿ�Ƕ�ף�true,�������parent�����ࣻfalse���������ļ�������ͨ�ࣨ��Ƕ���ࣩ��
	private boolean isInterface; //true:�ǽӿڣ���ʱ�����ǽӿڵĴ�����䶨λ��
	private boolean willRemove; //ɾ������Ƕ����ʱ��û�к��㷨������һ����־λ��
	/*
	 * ע�⣺ methods�����������ڲ��ࡢǶ���ࡢ�������inner class�ķ�����
	 */
	private List<MethodContext> methods; //��������з�����
	/*
	 * category=1,Java���ӿڣ� 
	 * category=10,C++��ͨ�࣬11, ����XX::yy���࣬�������޶����ţ�
	 *          12,C++�����ͨ����(���㺯�����������κ���); 
	 *          13,ȱ��ͷ�ļ����ࣻ 
	 *          14��C++ȫ�ֱ������ļ��������������䣬��ɵ�TopDeclartionCpp�ࡣ
	 *          15��ͷ�ļ������ļ�����һ��ǰ����parsingFilenameָ����������ProjectContext��Stringָ����
	 *          16,C++������
	 * category=20, C ����������ࡣ
	 */
	private int category; //�������
	//���¶�������������� category=15��������������ͷ�ļ���
	public final static String TopLevelFunction = "TopLevelFunction";//C++: category=12��13
	public final static String VirtualClassOfC = "VirtualClassOfC";//C: category=20
	public final static String TopDeclartionCpp = "TopDeclartionCpp";//C++: category=14
	
	public ClassContext()
	{
		super.setEndLine(0);
		super.setStartLine(0);
		name = "";
		parsingFilename = "";
		attributes = new ArrayList<>();
		methods = new ArrayList<>();
		parentName = "";
		nesting = false;
		isInterface = false;
		willRemove = false;
		category = 1; //ȱʡΪJava���ӿڣ�
	}

	//������ӿ�����
	public String getName() {
		return name;
	}

	//ָ��������ӿ�����
	public void setName(String name) {
		this.name = name;
	}

	//c���Գ���������ࡣ
	public void setVirtualNameCategory()
	{
		name = VirtualClassOfC;
		category = 20;
	}
	
	//c++���Գ����TopDeclartionCpp������������͡�
	public void setTopDeclartionNameCategory()
	{
		name = TopDeclartionCpp;
		category = 14;
	}

	//�������������𣿣�C++�����ռ�����C++ȫ�ֱ������ļ��������������䣩
	public boolean isTopDeclartionClass()
	{
		if( category==14 )
			return true;
		else
			return false;
	}
	
	//��������������ĸ��ļ�������Ŀ¼��Ϣ��
	public String getParsingFilename() {
		return parsingFilename;
	}

	public void setParsingFilename(String parsingFilename) {
		this.parsingFilename = parsingFilename;
	}

	 /*����ĺ���ר�ã���¼������������ĸ��ļ�,��ָ�������Ⱥ��
	  * filename :����Ŀ¼��Ϣ��
	  */
	public void setFuncParsingFilenameCategoy(String filename, int kind)
	{
		if( category==10 || category==15 )//�ú�������ķ��������������������parsingFilenameָ�����ļ��С���ʱ��parsingFilename��filename����ͬ��
			category = 15;//15��ͷ�ļ������ļ�����һ��ǰ����parsingFilenameָ����������ProjectContext��Stringָ����
		else 
		{ //����10 & 15����ʼֵ1������Java������10������C++��ͨ��,15 �������������ࣩ��˵�����Ǻ�����������ķ���������û�н��������ͷ�ļ���
			this.parsingFilename = filename;
			this.category = kind;
		}
	}
	
	//true:�ǽӿڣ�false �� �ࡣ
	public boolean isInterface() {
		return isInterface;
	}

	//ָ�����ͣ�true:�ǽӿڣ�false �� �ࡣ
	public void setInterface(boolean isInterface) {
		this.isInterface = isInterface;
	}
	
	//�����Ƕ���࣬����ʾ���ⲿ������nestingǶ�����־��
	public void setParentNode(String outtername)
	{
		nesting = true;
		parentName = outtername;
	}

	//��ȡoutter class������isNesting=true����ֵ�������塣
	public String getParentName() {
		return parentName;
	}

	//=true��˵�����ڲ���;false,��top-level�ࡣ
	public boolean isNesting() {
		return nesting;
	}

	//�������б����һ����������
	public void addAttribueName(String attributeName)
	{
		attributes.add(attributeName);
	}
	
	// ������������б�
	public List<String> getAttributes() {
		return attributes;
	}

	//��������з�����
	public List<MethodContext> getMethods() {
		return methods;
	}

	public void setMethods(List<MethodContext> methods) {
		this.methods = methods;
	}

	//�����б����һ��������
	public void addMethod(MethodContext mc)
	{
		methods.add(mc);
	}

	//ɾ������Ƕ����ʱ��û�к��㷨������һ����־λ��
	public boolean isWillRemove() {
		return willRemove;
	}

	//=true�������ᱻɾ����
	public void setWillRemove(boolean willRemove) {
		this.willRemove = willRemove;
	}
	
	/*
	 * category=1,Java���ӿڣ� 
	 * category=2,C++��ͨ�࣬3,C++�����ͨ����(�������κ���)��4��C++������
	 * category=5, C �������ͨ������
	 */
	public int getCategory() {
		return category;
	}

	public void setCategory(int category) {
		this.category = category;
	}

	/** ��ccNode�ڵ����з�����������У�����ķ������Գ������ⶼ���䣬���ͱ�Ϊ5.
	 * @param ccNode  1���������ļ�������ࣻ2��ccNode���ڴ����ڲ�����ڲ��ࡣ
	 */
	public void mergeInner2Class(ClassContext ccNode)
	{
		List<MethodContext> innerMethods = ccNode.getMethods();
		for( MethodContext mc: innerMethods )
		{
			mc.setInner2Type();
			methods.add(mc);
		}
	}
	
	//��һ��鷽���ڵ���䣬���ʹ����������ԣ���Ӧ�����Ӹ��Ӷ�.
	public void adjustComplexMetricWithAttribute()
	{
		for( MethodContext method :  methods )
			method.adjustComplexMetricWithAttribute(attributes);
	}
	
	/*��һ��鷽���ڵ���䣬���ʹ����������ԣ���Ӧ�����Ӹ��Ӷ�.
	 * outerAttributes�ⲿ��������б�
	 */
	public void adjustComplexMetricWithOutterAttribute(List<String> outerAttributes)
	{
		for( MethodContext method :  methods )
			method.adjustComplexMetricWithAttribute(outerAttributes);
	}
	
	/*��һ��鷽���ڵ���䣬���ʹ���˺궨�壬��Ӧ�����Ӹ��Ӷ�.
	 * outerAttributes�ⲿ��������б�
	 */
	public void adjustComplexMetricWithMacroDefinition(List<String> macroDefines)
	{
		for( MethodContext method :  methods )
			method.adjustComplexMetricWithMacroDefinition(macroDefines);
	}
	
	 /*
	  * ������C����������ࡣ�ڽ���ʱ����ȫ�ֱ�����Ϊ�˴��������
	  * �����з����������������ȫ�ֱ��������ԣ������ĸ��Ӷȣ�ȫ�ֱ����������Ҳ�����⡣
	  */
	public void cAdjustComplexMetricWithGlobalVariable()
	{
		for( MethodContext method :  methods )
			method.adjustComplexMetricWithAttribute(attributes);
	}
	
	/*
	  * ������C++���������TopDeclartionCpp�ࡣ�ڽ���ʱ����ȫ�ֱ�����Ϊ�˴��������
	  * �����з����������������ȫ�ֱ��������ԣ������ĸ��Ӷȣ�ȫ�ֱ����������Ҳ�����⡣
	  */
	public void cppAdjustComplexMetricWithGlobalVariable(List<String> globalVariables)
	{
		for( MethodContext method :  methods )
			method.adjustComplexMetricWithAttribute(globalVariables);
	}
	
	/**���кţ��ҳ���Ӧ����֪���Ӷ�
	 * @param lineno  �к�
	 * @return �к�δ�ҵ����򷵻�-1. lineno�϶����ڸ����У�����-3.
	 */
	public int getCognitiveComplexByLineno(int lineno)
	{
		int cognitive = -3;
		int startLineno = this.getStartLine();
		int endLineno = this.getEndLine();
		if( endLineno>startLineno )
		{ //endLineno��startLineno����ʼ��Ϊ0��category=12,13,14��20��Щ���ͣ����޷�����ʼ�кͽ����С�
			if( lineno<startLineno || lineno>endLineno )
				return cognitive;
		}
		cognitive = -1;
		for( MethodContext mCtx : methods )
		{
			int vcogn = mCtx.getCognitiveComplexByLineno(lineno);
			if( vcogn>=0 ) //����-1����ʾδ�ҵ���
			{
				cognitive = vcogn;
				break;
			}
		}
		return cognitive;
	}
	
	//��ʾ������Ϣ
	@Override
	public void showMe()
	{
		if( nesting )
			System.out.println("Filename: "+parsingFilename+"  Class:= "+name+",  Category="+category+", isInterface="+isInterface+", nesting, parent= "+parentName);
		else
			System.out.println("Filename: "+parsingFilename+"  Class:= "+name+",  Category="+category+", isInterface="+isInterface);
		//��ӡ�������б�
		System.out.print("        ");
		for( String attrname :attributes )
			System.out.print(attrname+",");
		System.out.println("         attirbute total = "+attributes.size());
		super.showMe();
		//��ʾ���������ݡ�
		for ( MethodContext mctxt : methods )
			mctxt.showMe();
	}
}
