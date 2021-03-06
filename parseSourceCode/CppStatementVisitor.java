/**
 * 
 */
package parseSourceCode;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansionLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroExpansion;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTBreakStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCaseStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCatchHandler;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTContinueStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarationStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDefaultStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDoStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTExpressionStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTForStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTGotoStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIfStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTReturnStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSwitchStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTWhileStatement;


/**
 * @author Administrator
 *
 */
public class CppStatementVisitor  extends ASTVisitor {
	private List<StatementContext> statementList; //????????????????????????????????????????????????????????AST????????????
	
	public CppStatementVisitor()
	{
		super(true);//public ASTVisitor(boolean visitNodes)
		statementList = new ArrayList<>();
	}
	//????????????????????
	public List<StatementContext> getStatementList() {
		return statementList;
	}
	
	/** 
	 *IASTStatement : ??????????????
	 * assert ???????? ??????????
	 * For??????????????????IASTForStatement.INITIALIZER??????????CPPASTDeclarationStatement??
	 * 		CPPASTExpressionStatement??????????visit????????????while,do while,if??????????????????for
	 *     ??????update??????????????visit??
	 */
	@Override
	public int visit(IASTStatement  statement) {
		try {
			if( statement instanceof CPPASTBreakStatement ) //break??????????????????????????????????
				return PROCESS_SKIP;
			else if( statement instanceof  CPPASTCatchHandler ) //catch...
				return 	ProcessCatchHandler((CPPASTCatchHandler)statement);
			else if( statement instanceof  CPPASTContinueStatement )  //continue??????????????????????????????????
				return 	PROCESS_SKIP;
			else if( statement instanceof  CPPASTDeclarationStatement )  //????????
			{
				ASTNodeProperty nodeProperty = statement.getPropertyInParent();
				//System.out.println(" ^^^^  "+nodeProperty.getName());
				//IASTWhileStatement.CONDITIONEXPRESSION   IASTIfStatement.CONDITION
				if( nodeProperty==IASTForStatement.INITIALIZER  ) //????????????????????
					return 	PROCESS_SKIP;
				return 	ProcessDeclarationStatement((CPPASTDeclarationStatement)statement);
			}
			else if( statement instanceof  CPPASTCaseStatement ) //case...
				return 	ProcessCaseStatement((CPPASTCaseStatement)statement);
			else if( statement instanceof  CPPASTDefaultStatement )  //Default??????????????????????????????????
				return 	PROCESS_SKIP;
			else if( statement instanceof  CPPASTDoStatement )  //do while
				return 	ProcessDoStatement((CPPASTDoStatement)statement);
			else if( statement instanceof  CPPASTWhileStatement )  //while
				return 	ProcessWhileStatement((CPPASTWhileStatement)statement);
			else if( statement instanceof  CPPASTForStatement )  //for...
				return 	ProcessForStatement((CPPASTForStatement)statement);
			else if( statement instanceof  CPPASTIfStatement )  //if...
				return 	ProcessIfStatement((CPPASTIfStatement)statement);
			else if( statement instanceof  CPPASTReturnStatement )  //return...
				return 	ProcessReturnStatement((CPPASTReturnStatement)statement);
			else if( statement instanceof  CPPASTSwitchStatement )  //switch...
				return 	ProcessSwitchStatement((CPPASTSwitchStatement)statement);
			else if( statement instanceof  CPPASTExpressionStatement ) //??????????????
			{
				ASTNodeProperty nodeProperty = statement.getPropertyInParent();
				if( nodeProperty==IASTForStatement.INITIALIZER  ) //??????????????????????
					return 	PROCESS_SKIP;
				return 	ProcessExpressionStatement((CPPASTExpressionStatement)statement);
			}
			else if( statement instanceof  CPPASTGotoStatement )  //goto ...
				return 	ProcessGotoStatement((CPPASTGotoStatement)statement);
			else //????????????
				return PROCESS_CONTINUE; 
		}//end of try
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Parse of statement is error.");
			System.out.println(statement.getRawSignature());
			return PROCESS_ABORT;
		}
	}
	
	/**  ????case ??????????????????????List<StatementContext> statementList??
	 * @param caseStmt
	 * @return case ????,??????????????????????????????
	 */
	private int ProcessCaseStatement(CPPASTCaseStatement caseStmt )
	{
		StatementContext stmtCtx = evaluateComplexMetric(caseStmt);
		stmtCtx.addSpeccialComplex(MetricCognitiveComplexity.CaseStmt);
		statementList.add(stmtCtx);
		return PROCESS_SKIP;
	}
	
	/**  ????????????????????????????????????List<StatementContext> statementList??
	 * @param expressStmt 
	 * @return ??????,??????????????????????????????
	 */
	private int ProcessExpressionStatement(CPPASTExpressionStatement expressStmt )
	{
		StatementContext stmtCtx = evaluateComplexMetric(expressStmt);
		statementList.add(stmtCtx);
		return PROCESS_SKIP;
	}
	
	/**  ??????????????????????????????????List<StatementContext> statementList??
	 * ??????????????????????????????????????????????????????????????????????????????????
	 * @param declarStmt 
	 * @return ??????,??????????????????????????????
	 */
	private int ProcessDeclarationStatement(CPPASTDeclarationStatement declarStmt )
	{
		StatementContext stmtCtx = evaluateComplexMetric(declarStmt);
		statementList.add(stmtCtx);
		return PROCESS_SKIP;
	}
	
	/**  catch handler??????????catch??????????????????????????????????????????
	 *      ????????????catch????????
	 * @param catchHandler 
	 * @return ????????????????????
	 */
	private int ProcessCatchHandler(CPPASTCatchHandler catchHandler )
	{
		StatementContext stmtContext = new StatementContext();

		IASTFileLocation  fileLocation = catchHandler.getFileLocation();
		//????????????????
		int startLine = fileLocation.getStartingLineNumber();
		int endLine = fileLocation.getEndingLineNumber();
		/*
		 * ????????????????handler??????????????
		 * 1??????catch(...)??????????)??????????{????????????catch??????????????????????????????
		 * 2, ????carch(other),??other??????????????catch??????????????????????????????
		 */
		if( catchHandler.isCatchAll() )
		{
			IASTStatement astBody = catchHandler.getCatchBody();
			fileLocation = astBody.getFileLocation();
			int bodyStartLine = fileLocation.getStartingLineNumber();
			if( endLine>bodyStartLine )
				endLine = bodyStartLine;
			stmtContext.addSpeccialComplex(MetricCognitiveComplexity.CppCatchAll);
		}
		else
		{
			IASTDeclaration astDeclar = catchHandler.getDeclaration();
			fileLocation = astDeclar.getFileLocation();
			endLine = fileLocation.getEndingLineNumber();
			stmtContext.addSpeccialComplex(MetricCognitiveComplexity.CatchCause);
			/*
			 * ????????????????catch????????????????????????????????????????????????
			 */
		}
		stmtContext.setStartLine(startLine);
		stmtContext.setEndLine(endLine);
		
		statementList.add(stmtContext);
		return PROCESS_CONTINUE; 
	}
	
	/**  for ????????
	 * @param forStatement 
	 * @return ??????????????????????
	 */
	private int ProcessForStatement(CPPASTForStatement forStatement )
	{
		boolean isSpecialFor = true;  //for(;;)????for????????ForInit??Expression??ForUpdate
		StatementContext forContext = new StatementContext();
		//for??????????????????
		IASTStatement initialStmt = forStatement.getInitializerStatement();
		if(  initialStmt!=null )
		{
			isSpecialFor = false; //??ForInit
			StatementContext statement = evaluateComplexMetric(initialStmt);
			forContext.enlargeToComplexStatement(statement);
		}
		//forContext
		IASTExpression 	forExpress = forStatement.getConditionExpression();
		if( forExpress!=null )
		{
			isSpecialFor = false; //??Expression
			StatementContext statement = evaluateComplexMetric(forExpress);
			forContext.enlargeToComplexStatement(statement);
		}
		//for??????update??????
		IASTExpression 	forUpdates = forStatement.getIterationExpression();
		if( forUpdates!=null )
		{
			isSpecialFor = false; //??ForUpdate
			StatementContext statement = evaluateComplexMetric(forUpdates);
			forContext.enlargeToComplexStatement(statement);
		}		
		//for????????????????????StatementContext??????????????????
		forContext.addSpeccialComplex(MetricCognitiveComplexity.LOOP);
		//????for?????? for ??????????????????????????????????for????????????????
		IASTFileLocation  fileLocation = forStatement.getFileLocation();
		int startLine = fileLocation.getStartingLineNumber();
		if( isSpecialFor )
		{ //for(;;)????for????????ForInit??Expression??ForUpdate
			forContext.setStartLine(startLine);
			forContext.setEndLine(startLine); //????????????????????????????????????????SBFL??
		}
		else
			forContext.enlargeStartLineno(startLine);
		statementList.add(forContext);
		return PROCESS_CONTINUE; //????????????????????
	}
	
	/**  do while??????do????????????????loop????????????????while??????????????
	 * @param doStatement 
	 * @return ??????????????????????
	 */
	private int ProcessDoStatement(CPPASTDoStatement doStatement )
	{
		IASTExpression 	astExpression = doStatement.getCondition();
		StatementContext stmtContext = evaluateComplexMetric(astExpression);
		
		//body????????????????????????????????????????????????????
		IASTStatement astBody = doStatement.getBody();
		IASTFileLocation fileLocation = astBody.getFileLocation();
		int bodyEndLine = fileLocation.getEndingLineNumber();
		stmtContext.addSpeccialComplex(MetricCognitiveComplexity.LOOP);
		stmtContext.enlargeStartLineno(bodyEndLine);
		statementList.add(stmtContext);
		return PROCESS_CONTINUE; //????????????????????
	}
	
	/**  if ??????then??????????????????????if??????????????
	 * else ????????if
	 * @param ifStatement 
	 * @return ??????????????????????
	 */
	private int ProcessIfStatement(CPPASTIfStatement ifStatement )
	{
		IASTExpression 	astExpression = ifStatement.getConditionExpression();
		StatementContext stmtCtx;
		if( astExpression==null )
		{  //??????	if( int ppp= Overview) ??????????????if????????????????????????????????
			IASTDeclaration astDeclar = ifStatement.getConditionDeclaration();
			stmtCtx= evaluateComplexMetric(astDeclar);
		}
		else
			stmtCtx= evaluateComplexMetric(astExpression);
		stmtCtx.addSpeccialComplex(MetricCognitiveComplexity.IfElse);
		//????if?????? if ??????????????????????????????????if()????????????????
		IASTFileLocation fileLocation = ifStatement.getFileLocation();
		int startLine = fileLocation.getStartingLineNumber();
		stmtCtx.enlargeStartLineno(startLine);
		statementList.add(stmtCtx);
		return PROCESS_CONTINUE; //????????????????????
	}
	
	/**  Return??????
	 * @param returnStmt 
	 * @return ??????,??????????????????????????????
	 */
	private int ProcessReturnStatement(CPPASTReturnStatement returnStmt )
	{
		StatementContext stmtCtx = evaluateComplexMetric(returnStmt);
		stmtCtx.addSpeccialComplex(MetricCognitiveComplexity.ReturnStmt);
		statementList.add(stmtCtx);
		return PROCESS_SKIP;//????????????????????????????????
	}
	
	/**  switch??????
	 * @param switchStmt 
	 * @return 
	 */
	private int ProcessSwitchStatement(CPPASTSwitchStatement switchStmt )
	{
		IASTExpression 	astExpression = switchStmt.getControllerExpression();
		StatementContext stmtCtx = evaluateComplexMetric(astExpression);
		stmtCtx.addSpeccialComplex(MetricCognitiveComplexity.SwitchStmt);
		//????switch?????? switch ??????????????????????????????????switch()????????????????
		IASTFileLocation fileLocation = switchStmt.getFileLocation();
		int startLine = fileLocation.getStartingLineNumber();
		stmtCtx.enlargeStartLineno(startLine);
		
		statementList.add(stmtCtx);
		return PROCESS_CONTINUE;//????????????????????
	}
	
	/**  while??????
	 * @param whileStmt 
	 * @return 
	 */
	private int ProcessWhileStatement(CPPASTWhileStatement whileStmt )
	{
		IASTExpression 	astExpression = whileStmt.getCondition();
		StatementContext stmtCtx;
		if( astExpression==null )
		{  //??????	while( int k=len++ ) ??????????????while????????????????????????????????
			IASTDeclaration astDeclar = whileStmt.getConditionDeclaration();
			stmtCtx= evaluateComplexMetric(astDeclar);
		}
		else
			stmtCtx= evaluateComplexMetric(astExpression);
		stmtCtx.addSpeccialComplex(MetricCognitiveComplexity.LOOP);
		//????while?????? while ??????????????????????????????????while()????????????????
		IASTFileLocation fileLocation = whileStmt.getFileLocation();
		int startLine = fileLocation.getStartingLineNumber();
		stmtCtx.enlargeStartLineno(startLine);
		
		statementList.add(stmtCtx);
		return PROCESS_CONTINUE;//????????????????????
	}
	
	/**  goto ??????
	 * @param gotoStmt 
	 * @return ??????,??????????????????????????????
	 */
	private int ProcessGotoStatement(CPPASTGotoStatement gotoStmt )
	{
		StatementContext stmtCtx = evaluateComplexMetric(gotoStmt);
		stmtCtx.addSpeccialComplex(MetricCognitiveComplexity.GotoStmt);
		statementList.add(stmtCtx);
		return PROCESS_SKIP;//????????????????????????????????
	}
	
	/**   ??????????????????????????????????????????????
	 * @param statement  ??????????????????????????if,for...
	 * @return
	 */
	private StatementContext evaluateComplexMetric(IASTStatement  statement)
	{
		return evaluateComplexMetric((IASTNode)statement);
	}
	
	/**   ????????????????????????????????????????????????
	 * @param expression  ??????????
	 * @return
	 */
	private StatementContext evaluateComplexMetric(IASTExpression  expression)
	{
		return evaluateComplexMetric((IASTNode)expression);
	}
	
	/**   ????????????????????????????????????????????????
	 * @param declaration  ????????
	 * @return
	 */
	private StatementContext evaluateComplexMetric(IASTDeclaration  declaration)
	{
		return evaluateComplexMetric((IASTNode)declaration);
	}
	
	/**   ????????????????????????????????????????????
	 * @param astNode  : ????????????????????????????...
	 * @return
	 */
	private StatementContext evaluateComplexMetric(IASTNode  astNode)
	{
		StatementContext stmtContext = new StatementContext();
		IASTFileLocation  fileLocation = astNode.getFileLocation();
		//????????????????
		int startLine = fileLocation.getStartingLineNumber();
		int endLine = fileLocation.getEndingLineNumber();
		stmtContext.setStartLine(startLine);
		stmtContext.setEndLine(endLine);
		CppFragmentVisitor fragmentVisitor= new CppFragmentVisitor();
		astNode.accept(fragmentVisitor);
		//????????????????????????????????
		fragmentVisitor.excludeInvoMethodFromIdentiferName();
		stmtContext.setSimpleNames(fragmentVisitor.getIdentiferNames());
		//????????????????????????????????????????????????????????simpleNames??????????????????
		IASTNodeLocation[] location = astNode.getNodeLocations();
		for (IASTNodeLocation loc : location) {
		    if (loc instanceof IASTMacroExpansionLocation) {
		    	IASTPreprocessorMacroExpansion iapmExpansion = ((IASTMacroExpansionLocation)loc).getExpansion();
		    	// IASTPreprocessorMacroDefinition imd= iapme.getMacroDefinition(); //<< returns the macro that generated "node"
		        IASTName macroName = iapmExpansion.getMacroReference();
		        stmtContext.addMacroDefinitionName(macroName.getLastName().toString());
		    }
		}
		//????????????++ -- ! != && || ????????????
		fragmentVisitor.adjustCognitiveComplexityWithInfixOperator();
		stmtContext.setCognitiveComplexity(fragmentVisitor.getMetric());
		return stmtContext;
	}
}
