/**
 * 
 */
package parseSourceCode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTBinaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTConditionalExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTFieldReference;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTFunctionCallExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTName;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTUnaryExpression;

/**  ����Ϊ��λ����C ���� ���Ӷȣ������Ǳ��ʽ���ͱ��������ڵ�Ƕ����䡣
 *  ������Ϊ��������visit���ҳ�������ڵĺ������á��߼���������ֲ�������������ȫ�ֱ����ȡ�
 * @author Administrator
 *
 */
public class ProcedureFragmentVisitor extends ASTVisitor {
	private int metric;  //���μ��������֪���Ӷ�
	List<String> identiferNames; //��¼���������б�ʶ�����ų������֡�����������������������
	List<String> invoMethods; //��¼��������ȫ�ֱ�������������������Ҫ��identiferNames��ɾ�����ǡ�
	List<String> logicOperators;  //�������������������ظ���&& || !
	List<String> strayOperators;  //���Infix,ǰ׺���ʽ����������׺���ʽ���������������ظ��� ++ -- ~ ^ & | 
	
	public ProcedureFragmentVisitor()
	{
		super(true);//public ASTVisitor(boolean visitNodes)
		
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
	
	/**
	 * ��IType getExpressionType() ���жϱ��ʽ�����
	 *IASTExpression : ���ռ��㸴�Ӷȵĸ��ֱ��ʽ����ʽ���š�
	 *��ʱ�޷�ʶ�� ���� class::field�ı��ʽ��
	 */
	@Override
	public int visit(IASTExpression  expression) {
		try {
			if( expression instanceof CASTConditionalExpression ) //�������ʽ��
				return ProcessConditionalExpression((CASTConditionalExpression)expression);
			else if( expression instanceof CASTBinaryExpression ) //��Ԫ���ʽ��
				return ProcessBinaryOperator((CASTBinaryExpression)expression);
			else if( expression instanceof CASTUnaryExpression ) //һԪ���ʽ��
				return ProcessUnaryOperator((CASTUnaryExpression)expression);
			else if( expression instanceof CASTFunctionCallExpression ) //�������ñ��ʽ��
				return ProcessFunctionCallExpression((CASTFunctionCallExpression)expression);
			/*ע�⣺CPPASTFunctionCallExpression�Ĳ������ݻᵱ�����á�
			 * ��ProcessFieldReferenceExpression�б����ظ�����*/
			else if( expression instanceof CASTFieldReference ) //���ñ��ʽ��
				return ProcessFieldReferenceExpression((CASTFieldReference)expression);
			else //����δ֪���
				return PROCESS_CONTINUE;
		}//end of try
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Parse of Fragment is error.");
			System.out.println(expression.getRawSignature());
			return PROCESS_ABORT;
		}
	}
	
	/**   ���Ӷ�  �������ʽ ? :
	 * @param condExpression  Conditional expression
	 * @return
	 */
	private int ProcessConditionalExpression(CASTConditionalExpression condExpression)
	{
		metric +=MetricCognitiveComplexity.ConditionExpress; 
		return PROCESS_CONTINUE; //����Ƕ��
	}
	
	/**  ��Ԫ���ʽ����
	 * @param binaryExpression
	 * @return ����skip���޷�visit( IASTName astName ) ����ʱֻ��continue;
	 */
	private int ProcessBinaryOperator(CASTBinaryExpression binaryExpression)
	{
		String strOpor ="";
		int rtnProcess = PROCESS_CONTINUE; //����Ƕ��
		int oOperator =binaryExpression.getOperator();
		if( oOperator== IASTBinaryExpression.op_logicalAnd )
			strOpor = "&&";
		else if( oOperator== IASTBinaryExpression.op_logicalOr )
			strOpor = "||";
		else if( oOperator== IASTBinaryExpression.op_binaryXor    ) //oOperator==IASTBinaryExpression.op_binaryXorAssign
			strOpor = "^";
		else if( oOperator== IASTBinaryExpression.op_binaryOr    ) //oOperator==IASTBinaryExpression.op_binaryOrAssign
			strOpor = "|";
		else if( oOperator== IASTBinaryExpression.op_binaryAnd    ) //oOperator==IASTBinaryExpression.op_binaryAndAssign
			strOpor = "&";
		else 
			return rtnProcess; //�������Ͳ����Ӹ��Ӷȡ�
		if( oOperator== IASTBinaryExpression.op_logicalAnd || oOperator== IASTBinaryExpression.op_logicalOr )
		{
			if( !isExistOperator(logicOperators,strOpor) )
				logicOperators.add(strOpor);
		}
		else 
		{
			if( !isExistOperator(strayOperators,strOpor) )
				strayOperators.add(strOpor);
		}
		return rtnProcess;
	}
	
	/**  һԪ���ʽ����
	 * @param binaryExpression
	 * @return ����skip���޷�visit( IASTName astName ) ����ʱֻ��continue;
	 */
	private int ProcessUnaryOperator(CASTUnaryExpression unaryExpression)
	{
		String strOpor ="";
		int rtnProcess = PROCESS_CONTINUE;//����Ƕ��
		int oOperator =unaryExpression.getOperator();
		if( oOperator== IASTUnaryExpression.op_postFixIncr || oOperator== IASTUnaryExpression.op_prefixIncr )
			strOpor = "++";
		else if( oOperator== IASTUnaryExpression.op_postFixDecr || oOperator== IASTUnaryExpression.op_prefixDecr )
			strOpor = "--";
		else if( oOperator== IASTUnaryExpression.op_not )
			strOpor = "!";
		else if( oOperator== IASTUnaryExpression.op_tilde )
			strOpor = "~";
		else 
			return rtnProcess; //�������Ͳ����Ӹ��Ӷȡ�
		if( oOperator== IASTUnaryExpression.op_not )
		{
			if( !isExistOperator(logicOperators,strOpor) )
				logicOperators.add(strOpor);
		}
		else 
		{
			if( !isExistOperator(strayOperators,strOpor) )
				strayOperators.add(strOpor);
		}
		return rtnProcess;
	}
	
	/**   ���Ӷ�  �������ã��������ڲ��������ú��ⲿ�������ã��Ӹ��Ӷȡ�
	 * @param funcExpression   Function Call Expression
	 * @return
	 */
	private int ProcessFunctionCallExpression(CASTFunctionCallExpression funcExpression)
	{
		//metric +=MetricCognitiveComplexity.CppFuncCall; 
		IASTExpression fnameExpress = funcExpression.getFunctionNameExpression();
		EnumInvocationIdentifer(fnameExpress);
		metric +=MetricCognitiveComplexity.CFuncCall;
		return PROCESS_CONTINUE; //����Ƕ��
	}
	
	/**  ����(����ṹ��ö�ٵ�)�ĸ��Ӷ�.
	 * This interface represents expressions that access a field reference. 
	 * e.g. a.b => a is the expression, b is the field name. 
	 * @param frefExpression   Field Reference
	 * @return
	 */
	private int ProcessFieldReferenceExpression(CASTFieldReference frefExpression)
	{
		metric +=MetricCognitiveComplexity.CFieldReference;
		/*
		 * ��ΪIASTExpression 	astExpress�϶��ᱻ�����������ֲ���������������Լ��㸴�Ӷȡ�
		 * ���Խ�������invoMethods�����ظ����㡣
		 */
		IASTName astName = frefExpression.getFieldName();
		addInvoMethod(astName.toString());
		IASTExpression 	astExpress = frefExpression.getFieldOwner();
		EnumInvocationIdentifer(astExpress);
		return PROCESS_CONTINUE; //����Ƕ�ף�A.B.C������Ƕ�������ظ����㸴�Ӷȡ�
	}
	
	/**  �ҳ����ʽ����ı��������������ȣ����뵽invoMethods
	 * @param fnExpress
	 * @return ���ʽ����ı�ʶ��������
	 */
	private void EnumInvocationIdentifer(IASTExpression iastExpress)
	{
		iastExpress.accept(  new ASTVisitor() { 
			{
		        super.shouldVisitNames = true;         // Set this flag to visit names.
			}
			
			@Override
			public int visit( IASTName astName ) 
			{
				if( astName instanceof CASTName )
				{
					char[] sname = ((CASTName)astName).getSimpleID();
					//System.out.println("            "+new String(sname));
					addInvoMethod(new String(sname));
				}
				else{
				}
				return PROCESS_SKIP; //���������ӽڵ㡣
			} //end of visit
		}//end of ASTVisitor	
		); //end of accept.
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
	
	/**
	 * �����C���ƣ������������������ȡ�
	 */
	@Override
	public int visit( IASTName astName ) {
		if( astName instanceof CASTName )
		{
			char[] sname = ((CASTName)astName).getSimpleID();
			addIdentiferName(new String(sname));
		}
		else
		{
			//System.out.println("##    "+astName.getLastName().toString());
		}
		return PROCESS_SKIP; //���������ӽڵ㡣
	}
	

}
