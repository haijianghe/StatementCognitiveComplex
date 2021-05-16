/**
 * 
 */
package parseSourceCode;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/** ������Ŀ�Ľ������ݡ�
 * @author Administrator
 *
 */
/**
 * @author Administrator
 * ע�⣺ �����ļ�������ΪMap�����key.
 */
public class ProjectContext {
	private int language; //1=C,2=C++,3=Java
	/*��¼�������н������ݡ�һ����˵��һ���ļ�ֻ��һ���࣬Ҳ����˵List<ClassNode>ֻ�е�������
	 *���C���ԣ�������ԼΪ�ļ�������������ԼΪ�ļ��ڵ�ȫ�ֱ����� 
	 *C���Ժ�C++��ͷ�ļ������ļ����ֿܷ���String���ļ�����ָ���ļ�,��ClassContext��parsingFilename���ǽ���ʱ�����ĵ�һ���ļ�����
	 *��Java��˵���򲻴��ڴ����⡣String���ļ�����ClassContext��parsingFilename��ͬһ����������ʱ���ࡣ
	 */
	Map<String,List<ClassContext>>  codeContexts;  
	
	
	//���캯����
	public ProjectContext(int type)
	{
		this.language = type;
		codeContexts = new HashMap<String,List<ClassContext>>();
	}
	
	public ProjectContext(String codeType)
	{
		if( codeType.contentEquals("C") )
			this.language = 1;
		else if( codeType.contentEquals("C++") )
			this.language = 2;
		else if( codeType.contentEquals("Java") )
			this.language = 3;
		else
			this.language = 0;
		codeContexts = new HashMap<String,List<ClassContext>>();
	}
	
	/** ���һ���ļ��������
	 * @param pathName  ��������Դ�����ļ�,��Ŀ¼��Ϣ��
	 * @param clazzLst  ���ļ��Ľ��������
	 */
	public void addClazzList(String pathName,List<ClassContext> clazzLst)
	{
		codeContexts.put(getFilenameFromPathFile(pathName), clazzLst);
	}
	
	
	//���ļ�����ȡ����������
	public List<ClassContext> getClassNodeFromFile(String filename)
	{
		return codeContexts.get(filename);
	}
	
	
	/** �Ӵ�·�����ļ�����ȡ������ļ�����
	 * @param pathName ��·�����ļ���
	 * @return
	 */
	public static String getFilenameFromPathFile(String pathName)
	{
		int pos = pathName.lastIndexOf('\\');
		String filename = pathName.substring(pos+1);
		return filename;
	}
	
	/** ���ļ��������кţ��ҳ���Ӧ����֪���Ӷ�
	 * @param filename  �ļ���
	 * @param lineno    �к�
	 * @return ���Ӷ�ֵ���ļ�δ�ҵ����򷵻�-4.(�������Ҳ�����-3�����з����Ҳ���-2��)�к�δ�ҵ����򷵻�-1.
	 */
	public int getCognitiveComplexByFileLineno(String filename,int lineno) 
	{
		int cognitive = -4;
		for(Map.Entry<String, List<ClassContext>>  entry  :  codeContexts.entrySet()){
			String parsingFilename = entry.getKey();
			if( !parsingFilename.contentEquals(filename) )
				continue;
			//�ļ���ƥ��
			cognitive = -1; //��Ȼ�ļ��ҵ��������Ҳ����кš�
			List<ClassContext> ctxLst = entry.getValue();
			for( ClassContext cContext : ctxLst )
			{
				int vcogn = cContext.getCognitiveComplexByLineno(lineno);
				if( vcogn>=0 ) //-1��ʾδ�ҵ���
				{
					cognitive = vcogn;
					break;
				}
			}//end of for...
			break;//�ļ��ҵ����к��Ҳ���;�����Ѿ��������Ӷȡ�����������������˳�ѭ��
		}
		return cognitive;
	}
}
