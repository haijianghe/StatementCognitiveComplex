/**
 * 
 */
package common;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import affiliated.AffiliatedFactory;
import affiliated.ExcludeVersion;
import affiliated.FileSpectrum;
import affiliated.IFaultFile;
import affiliated.IProfileFile;
import softComplexMetric.ISoftwareMetric;
import softComplexMetric.LineCognitiveComplex;

/** �������ĸ��Ӷȣ��洢����Ӧ�ļ���
 * @author Administrator
 *
 */
public class SoftwareMetricGeneration {

	/**
	 * ���㣬�������������ݼ��ģ�����Ϊ��λ����֪���Ӷȡ�
	 */
	public static void calculateStoreCongnitiveMetric()
	{
		List<String> allObjectNames = XMLConfigFile.getAllObjectNames();
		for( String project : allObjectNames )
		{
			//PairikaOpenCV331 Math FasterXML print_tokens
			//if( !project.contentEquals("PairikaOpenCV331") )  //test.
			//	continue;
			System.out.println(project+" start. ");
			boolean processok = calculateStoreCongnitiveMetric(project);
			if( processok )
				System.out.println("\n    @@"+project+" 's parsing process is ok.");
			else
				System.out.println("\n    @@"+project+" 's parsing process is fail. Check it.");
		}
	}
	
	/**��Ȩ����������ΪCSDN���������������ڡ���ԭ�����£���ѭCC 4.0 BY-SA��ȨЭ�飬ת���븽��ԭ�ĳ������Ӽ���������
	ԭ�����ӣ�https://blog.csdn.net/m0_38059938/article/details/80658409
	 * ����String��ʱ�䣬��ȡlong��ʱ�䣬��λ����
	 * @param inVal ʱ���ַ���
	 * @return long��ʱ��
	 */
	public static long fromDateStringToLong(String inVal) {
	    Date date = null;
	    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS");
	    try {
	        date = inputFormat.parse(inVal);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return date.getTime();
	}
	
	/** ����ض��������ơ�
	 * ���㣬�������������ݼ��ģ�����Ϊ��λ����֪���Ӷȡ�
	 */
	private static boolean calculateStoreCongnitiveMetric(String objectName)
	{
		boolean processok = true;
		//�ȶ�ȡ�ö���İ汾������
		IFaultFile ffiAgent = AffiliatedFactory.createFaultFileObject(objectName);
		if( !ffiAgent.readFaultFile() )
			return false;
		int timeCounts = 0;  //����ƽ��֮��Ĵ�����
		long timeCalCogn = 0; //��֪���Ӷ�ƽ���ļ���ʱ�䡣
		int vers = ffiAgent.getVerNo();
		for( int v=1;v<=vers; v++)
		{
			//if( v!=11 )
			//	continue;  //testing
			int bugId = ffiAgent.getBugID(v);
			if( true==ExcludeVersion.isExcludeVer(objectName,bugId) )
				continue; //�ð汾���μӼ��㡣
			//��ȡ.profile�ļ���
			//int[] faultStats = ffiAgent.getFaultLinesVer(v); //�ð汾�Ĺ�����������
			//String[] faultFilenames = ffiAgent.getFaultFilesVer(v);//�ð汾�Ĺ����ļ������顣
			IProfileFile profileAgent = AffiliatedFactory.createProfileFileObject(objectName, bugId);
			if( false==profileAgent.readProfileFile() )
			{
				processok = false;
				System.out.println("Read file "+objectName+"_v"+String.valueOf(bugId)+".profile is error.");
				break;
			}//end of if...
			timeCounts++;
			//��ȡ��ǰʱ��Ϊ��ֹʱ�䣬ת��Ϊlong��
			long startTime =fromDateStringToLong(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS").format(new Date()));
			//ע�⣺ �Ժ������ù���ģʽ���������Ӷȵļ��㣬�����ࡣ
			ISoftwareMetric sMetric = new LineCognitiveComplex(objectName,bugId);
			boolean calResult = sMetric.calComplexMetricValue();
			if( !calResult  )
			{
				processok = false;
				break;
			}
			//��ȡ��ǰʱ��Ϊ��ʼʱ�䣬ת��Ϊlong��
			long stopTime = fromDateStringToLong(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS").format(new Date()));
			//����ʱ���,��λ����
			timeCalCogn += stopTime - startTime;
			//������ȷ������������.cognitive�ļ�
			List<FileSpectrum> fileSpectra = profileAgent.getSpectrumList();
			boolean writeResult = sMetric.writeCognitiveComplexFile(fileSpectra);
			if( !writeResult )
			{
				processok = false;
				break;
			}
		}//end of for...
		System.out.println("\n%$^#@*!"+objectName+" 's time of calculate cognitive complex is "+timeCalCogn/timeCounts+"ms");
		return processok;
	}
}
