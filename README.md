# StatementCognitiveComplex
The source code of statement level cognitive complex.

计算认知复杂度，用到Eclipse的AST（抽象语法树） Parser解析源程序。C程序和C++程序的解析调用CDT组件，Java程序的解析调用JDT组件。
文件pom.xml，包含Maven工程需要的信息，能自动找到这些组件。

调用common包里的SoftwareMetricGeneration.calculateStoreCongnitiveMetric();即可计算认知复杂度。

这里是完整的认知复杂度计算源代码。
不过，缺乏一些软件故障定位计算的源代码，无法直接打开整个Eclipse程序。
这不会影响认知复杂度计算源代码的完整性。
