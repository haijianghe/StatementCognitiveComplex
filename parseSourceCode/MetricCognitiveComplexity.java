package parseSourceCode;

/**  Ϊ��ͬ������֪���Ӷȶ���ֵ��
 * @author Administrator
 *
 */
public class MetricCognitiveComplexity {
	public static int LocalVariable = 1; //�����ľֲ�����
	public static int MethodParameter =2; //�����Ĳ���
	public static int AttributeExtern = 3; //������Ի���C/C++�ⲿ����
	public static int LOOP = 6;  //for ,while, do while,enhance for
	public static int ConditionExpress = 3; //��Ԫ�����
	public static int IfElse = 4;  //if, else if
	public static int CatchCause = 3; //try, catch
	public static int SwitchStmt = 4; //switch
	public static int ThrowStmt = 3;   //throw
	public static int CaseStmt = 2;    //case
	public static int ReturnStmt = 2;  //return 
	public static int InnerMethod = 2; //�ڲ�����
	public static int SuperMethod = 2; //���෽��
	public static int ExternMethod = 3; //�ⲿ����
	public static int RefCreation =2 ;//Creation Reference
	public static int RefExMethod = 3;  //ExpressionMethodReference
	public static int RefSupMethod = 3;  //SuperMethodReference
	public static int RefTypMethod = 3;  //TypeMethodReference
	public static int SynchronizedStmt =4; //Synchronized���
	public static int LambdalExp = 3;  //LambdaExpression ���
	public static int Unit = 1; //���ӶȵĻ�����Ԫ
	public static int GotoStmt = 2; //goto ���
/*This interface represents expressions that access a field reference. 
 * e.g. a.b => a is the expression, b is the field name. 
 * e.g. a()->def => a() is the expression, def is the field name. */
	public static int CppFieldReference = 3; //C++������У�Java��ʱû������ע�⣬���õı��ʽa������㸴�Ӷ�.
	public static int CppFuncCall = 3; //C++��ʱ�������ڲ��������ⲿ������ͳһ�ø��Ӷ�3.
	public static int CppCatchAll = 2; //C++��catch all��䡣 
	public static int CFuncCall = 3;  //C����ķ������á�
	public static int CFieldReference = 2; //C����������У�Java��ʱû������ע�⣬���õı��ʽa������㸴�Ӷ�.
	public static int CCPPMacroDefinition = 2; //C ��C++�ĺ궨�塣
}
