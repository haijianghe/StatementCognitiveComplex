package parseSourceCode;

/**  为不同语句的认知复杂度定义值。
 * @author Administrator
 *
 */
public class MetricCognitiveComplexity {
	public static int LocalVariable = 1; //方法的局部变量
	public static int MethodParameter =2; //方法的参数
	public static int AttributeExtern = 3; //类的属性或者C/C++外部变量
	public static int LOOP = 6;  //for ,while, do while,enhance for
	public static int ConditionExpress = 3; //三元运算符
	public static int IfElse = 4;  //if, else if
	public static int CatchCause = 3; //try, catch
	public static int SwitchStmt = 4; //switch
	public static int ThrowStmt = 3;   //throw
	public static int CaseStmt = 2;    //case
	public static int ReturnStmt = 2;  //return 
	public static int InnerMethod = 2; //内部方法
	public static int SuperMethod = 2; //父类方法
	public static int ExternMethod = 3; //外部方法
	public static int RefCreation =2 ;//Creation Reference
	public static int RefExMethod = 3;  //ExpressionMethodReference
	public static int RefSupMethod = 3;  //SuperMethodReference
	public static int RefTypMethod = 3;  //TypeMethodReference
	public static int SynchronizedStmt =4; //Synchronized语句
	public static int LambdalExp = 3;  //LambdaExpression 语句
	public static int Unit = 1; //复杂度的基本单元
	public static int GotoStmt = 2; //goto 语句
/*This interface represents expressions that access a field reference. 
 * e.g. a.b => a is the expression, b is the field name. 
 * e.g. a()->def => a() is the expression, def is the field name. */
	public static int CppFieldReference = 3; //C++程序才有，Java暂时没有做。注意，引用的表达式a不会计算复杂度.
	public static int CppFuncCall = 3; //C++暂时不区分内部方法和外部函数，统一用复杂度3.
	public static int CppCatchAll = 2; //C++的catch all语句。 
	public static int CFuncCall = 3;  //C程序的方法调用。
	public static int CFieldReference = 2; //C程序的引用有，Java暂时没有做。注意，引用的表达式a不会计算复杂度.
	public static int CCPPMacroDefinition = 2; //C 和C++的宏定义。
}
