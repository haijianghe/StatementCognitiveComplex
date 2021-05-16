/**
 * 
 */
package parseSourceCode;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTQualifiedName;

/**
 * @author Administrator
 *
 */
public class CppSimpleNameVisitor extends ASTVisitor {
	/*
	 * 这里有重复内容没有关系，在外面，表达式部分去重。
	 */
	List<String> simpleNames; //记录该表达式的所有标识符。
	
	public CppSimpleNameVisitor()
	{
		super(true);//public ASTVisitor(boolean visitNodes)
		
		simpleNames = new ArrayList<>();
	}	
	
	//记录该语句的所有标识符。排除保留字、函数名、类名、方法名。
	public List<String> getSimpleNames() {
		return simpleNames;
	}
	
	/**
	 * 语句中C++名称，包括变量名、类名等。
	 */
	@Override
	public int visit( IASTName astName ) {
		if( astName instanceof CPPASTName )
		{
			char[] sname = ((CPPASTName)astName).getSimpleID();
			simpleNames.add(new String(sname));
			//System.out.println("            "+new String(sname));
		}
		else if( astName instanceof CPPASTQualifiedName )
		{
			@SuppressWarnings("deprecation")
			IASTName[] 	subNames = ((CPPASTQualifiedName)astName).getNames();
			//System.out.print("           ");
			for( IASTName icans : subNames )
			{
				if( icans instanceof CPPASTName )
				{
					char[] sname = ((CPPASTName)icans).getSimpleID();
					simpleNames.add(new String(sname));
				}
				//CPPASTTemplateId类型的也会成为CPPASTQualifiedName的一部分，而CPPASTTemplateId无法转换为CPPASTName;
			}
			//System.out.println(" ");
		}

		else
		{
			/*
			 *  CPPASTConversionName, CPPASTImplicitName, CPPASTName, 
			 *  CPPASTNameBase, CPPASTOperatorName, CPPASTQualifiedName, CPPASTTemplateId
			 */
		}
		return PROCESS_SKIP; //不再搜索子节点。
	}
}
