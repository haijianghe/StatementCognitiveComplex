/**
 * 
 */
package parseSourceCode;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/** 整个项目的解析数据。
 * @author Administrator
 *
 */
/**
 * @author Administrator
 * 注意： 解析文件名会作为Map对象的key.
 */
public class ProjectContext {
	private int language; //1=C,2=C++,3=Java
	/*记录工程所有解析数据。一般来说，一个文件只有一个类，也就是说List<ClassNode>只有单个对象。
	 *针对C语言，类名规约为文件名；属性名规约为文件内的全局变量。 
	 *C语言和C++，头文件和主文件可能分开，String的文件名是指主文件,而ClassContext的parsingFilename则是解析时遇到的第一个文件名；
	 *对Java来说，则不存在此问题。String的文件名和ClassContext的parsingFilename是同一个东西，此时冗余。
	 */
	Map<String,List<ClassContext>>  codeContexts;  
	
	
	//构造函数。
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
	
	/** 添加一个文件解析结果
	 * @param pathName  被解析的源程序文件,带目录信息。
	 * @param clazzLst  该文件的解析结果。
	 */
	public void addClazzList(String pathName,List<ClassContext> clazzLst)
	{
		codeContexts.put(getFilenameFromPathFile(pathName), clazzLst);
	}
	
	
	//由文件名获取其解析结果。
	public List<ClassContext> getClassNodeFromFile(String filename)
	{
		return codeContexts.get(filename);
	}
	
	
	/** 从带路径的文件名中取纯粹的文件名。
	 * @param pathName 带路径的文件名
	 * @return
	 */
	public static String getFilenameFromPathFile(String pathName)
	{
		int pos = pathName.lastIndexOf('\\');
		String filename = pathName.substring(pos+1);
		return filename;
	}
	
	/** 由文件名和其行号，找出对应的认知复杂度
	 * @param filename  文件名
	 * @param lineno    行号
	 * @return 复杂度值，文件未找到，则返回-4.(所有类找不到，-3；所有方法找不到-2；)行号未找到，则返回-1.
	 */
	public int getCognitiveComplexByFileLineno(String filename,int lineno) 
	{
		int cognitive = -4;
		for(Map.Entry<String, List<ClassContext>>  entry  :  codeContexts.entrySet()){
			String parsingFilename = entry.getKey();
			if( !parsingFilename.contentEquals(filename) )
				continue;
			//文件名匹配
			cognitive = -1; //虽然文件找到，可能找不到行号。
			List<ClassContext> ctxLst = entry.getValue();
			for( ClassContext cContext : ctxLst )
			{
				int vcogn = cContext.getCognitiveComplexByLineno(lineno);
				if( vcogn>=0 ) //-1表示未找到。
				{
					cognitive = vcogn;
					break;
				}
			}//end of for...
			break;//文件找到，行号找不到;或者已经读到复杂度。无论哪种情况，都退出循环
		}
		return cognitive;
	}
}
