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
	 * ����궨�塢�ڲ����������������ԡ�ȫ�ֱ����Ը��Ӷȵ�Ӱ��ʱ��
	 * ��AccumulationComplex=false; ��ÿ��������ͬ���͵������ֻ����һ�Σ�
	 * ���磬ʹ���˶��������ֻ����һ�β����ĸ��Ӷȡ�
	 */
	private static boolean AccumulationComplex=true; 
	private boolean bLocalVaiable;  //������Ƿ������ӹ��ֲ������ĸ��Ӷ�ֵ 
	private boolean bMacroDefine;   //������Ƿ������ӹ��궨��ĸ��Ӷ�ֵ 
	private boolean bParameter;     //������Ƿ������ӹ������ĸ��Ӷ�ֵ 
	private boolean bAttribute;     //������Ƿ������ӹ����ԡ�ȫ�ֱ����ĸ��Ӷ�ֵ 
	
	private int cognitiveComplexity; //��֪���Ӷȣ�����Ϊ��λ��
	/*�����ھֲ�������ÿһ����������֪���Ӷ�1
	 * �����Ĳ���������ÿһ����������֪���Ӷ�2
	 * ������Ի򷽷����ⲿ������ÿһ����������֪���Ӷ�3
	 * */
	List<String> simpleNames; //��¼���������б�ʶ�����ų������֡�����������������������
	/*
	 *���ԣ� �ֲ�������������������������󣬻��simpleNamesɾ����
	 *���ԣ�һ������£�simpleNamesΪ�ա�
	 *���ǣ���for��while��������棬���Զ����ڲ���������Щ��������Ӱ�츴�Ӷȣ���������simpleNames�
	 *���У�����ֲ��������������Ͳ���ԭ���ͣ��ڵ������ķ���ʱ������Ա���������֪���Ӷȣ����ұ�����������simpleNames�
	 *     �磺 �оֲ�����SimpleSample ssm;   ssm.get();
	 *     get()�����и��Ӷȣ���ssm�Ǿֲ������и��Ӷȣ�
	 *     �������޸Ĵ��룬���Ľ����ssm�������㸴�Ӷȣ�������������simpleNames�
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
	
	//��֪���Ӷȣ�����Ϊ��λ��
	public int getCognitiveComplexity() {
		return cognitiveComplexity;
	}


	public void setCognitiveComplexity(int cognitiveComplexity) {
		this.cognitiveComplexity = cognitiveComplexity;
	}

	//��¼���������б�ʶ�����ų������֡�����������������������
	public List<String> getSimpleNames() {
		return simpleNames;
	}


	public void setSimpleNames(List<String> simpleNames) {
		this.simpleNames = simpleNames;
	}

	/** Ϊfor,while,do while���������Ӹ��Ӷȡ�
	 * @param complexMetric
	 */
	public void addSpeccialComplex(int complexMetric)
	{
		cognitiveComplexity += complexMetric;
	}
	
	/** for����� for����һ�У�while����� while����һ�� 
	 * ��������£�for(while,if, foreach,....)���ڵ��кŲ�������������ʼ�кţ����ô˷����󣬿ɽ���������⡣
	 * @param lineno
	 */
	public void enlargeStartLineno(int lineno)
	{
		if( getStartLine()>lineno )
			setStartLine(lineno);
	}
	
	/**  simpleNames�Ƿ����ĳ����ʾ��������������������...��
	 * @param identifer
	 * @return true: ���ڣ�
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
	/** ����һ�����ӵ���䣬����������֣��������չ���Ա�����µ�����sContext
	 * ��չ���ݰ����� ��ʼ����չ����������չ����֪���Ӷ��ۼӣ�simpleNames�ۼӡ�
	 * @param sContext ��һ������һ���֣��������
	 */
	public void enlargeToComplexStatement(StatementContext sContext)
	{
		//��ʼ�У���������չ
		int scstart = sContext.getStartLine();
		int scend = sContext.getEndLine();
		if( getStartLine()==0 || getEndLine()==0 )
		{ //������0��˵������仹δ��AST��
			setStartLine(scstart);
			setEndLine(scend);
			cognitiveComplexity = sContext.getCognitiveComplexity();
			simpleNames = sContext.getSimpleNames();
		}
		else
		{
			//��ʼ����չ����С���к�
			if( getStartLine()>scstart )
				setStartLine(scstart);
			//��������չ���Ҵ���к�
			if( getEndLine()<scend )
				setEndLine(scend);
			//��֪���Ӷ��ۼ�
			cognitiveComplexity += sContext.getCognitiveComplexity();
			//simpleNames�ۼӣ������ظ��� 
			List<String> simpleOfSC = sContext.getSimpleNames();
			for( String item : simpleOfSC)
			{
				if( !isExistSimpleNames(item) )
					simpleNames.add(item);
			}
		}//end of else
	}
	
	//���ƶ��ĺ궨�壬��������simpleNames��������Ӹ��Ӷȡ�
	public void addMacroDefinitionName(String macroName)
	{
		if( !isExistSimpleNames(macroName) )
			simpleNames.add(macroName);
	}
	
	//��һ��鷽���ڵ���䣬���ʹ���˾ֲ�������ÿʹ��һ������ͬ�������ʹ�ã���Ϊһ���������Ӹ��Ӷ�1.
	//methodVariable �����ڵľֲ�������
	public void adjustComplexMetricWithVariable(String methodVariable)
	{
		//����������ۼӣ������ۼӣ���ǰ��û�мӹ��ֲ������ĸ��Ӷ�ֵ. 
		if( (AccumulationComplex==false) && (bLocalVaiable==true) ) 
			return;
		for( Iterator<String> iterator = simpleNames.iterator(); iterator.hasNext();)
		{
			String snames = iterator.next();
			if( snames.contentEquals(methodVariable) )
			{
				cognitiveComplexity += MetricCognitiveComplexity.LocalVariable;
				iterator.remove();
				bLocalVaiable = true;//�����ӹ��ֲ������ĸ��Ӷ�ֵ 
				break;
			}//end of if
		}//end of for...
		
	}
	
	//��һ��鷽���ڵ���䣬���ʹ���˲�����ÿʹ��һ������ͬ�������ʹ�ã���Ϊһ���������Ӹ��Ӷ�2.
	//methodPara �����Ĳ�����
	public void adjustComplexMetricWithParameter(String methodPara)
	{
		//����������ۼӣ������ۼӣ���ǰ��û�мӹ������ĸ��Ӷ�ֵ. 
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
	
	/*��һ��鷽���ڵ���䣬���ʹ����������ԣ�ÿʹ��һ������ͬ���Զ��ʹ�ã���Ϊһ���������Ӹ��Ӷ�3.
	 *classAttr �����Ĳ�����
	 *ע�⣺ �ֲ��������ȼ���ߣ������ȵ���adjustComplexMetricWithVariable
	 *       ������֮��  ���ŵ���adjustComplexMetricWithParameter
	 *       ��������ٴ�֮�������ܵ���adjustComplexMetricWithAttribute
	 */
	public void adjustComplexMetricWithAttribute(String classAttr)
	{
		//����������ۼӣ������ۼӣ���ǰ��û�мӹ�ȫ�ֱ��������Եĸ��Ӷ�ֵ. 
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
	
	/*��һ��鷽���ڵ���䣬���ʹ���˺궨�壬ÿʹ��һ������ͬ���Զ��ʹ�ã���Ϊһ���������Ӹ��Ӷ�2.
	 *macroDefine �궨������
	 * �����ֶ�����ʽ�ĺ궨��PASTObjectMacro�ͺ�����ʽ�ĺ궨��PASTFunctionMacro,
	 */
	public void adjustComplexMetricWithMacroDefinition(String macroDefine)
	{
		//����������ۼӣ������ۼӣ���ǰ��û�мӹ��궨��ĸ��Ӷ�ֵ. 
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
	
	//��ʾ������Ϣ
	@Override
	public void showMe()
	{
		System.out.print("        ("+getStartLine()+","+getEndLine()+")="+cognitiveComplexity);
		System.out.print("        ");
		//��ӡsimpleNames�б�
		for( String para : simpleNames )
			System.out.print(para+",  ");
		System.out.println("=> "+simpleNames.size());
	}
}
