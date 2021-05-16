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
	 * �������ظ�����û�й�ϵ�������棬���ʽ����ȥ�ء�
	 */
	List<String> simpleNames; //��¼�ñ��ʽ�����б�ʶ����
	
	public CppSimpleNameVisitor()
	{
		super(true);//public ASTVisitor(boolean visitNodes)
		
		simpleNames = new ArrayList<>();
	}	
	
	//��¼���������б�ʶ�����ų������֡�����������������������
	public List<String> getSimpleNames() {
		return simpleNames;
	}
	
	/**
	 * �����C++���ƣ������������������ȡ�
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
				//CPPASTTemplateId���͵�Ҳ���ΪCPPASTQualifiedName��һ���֣���CPPASTTemplateId�޷�ת��ΪCPPASTName;
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
		return PROCESS_SKIP; //���������ӽڵ㡣
	}
}
