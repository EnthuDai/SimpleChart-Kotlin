# 一款轻量级android图表组件SimpleChart-Kotlin

# 效果演示

![基础效果演示](https://img-blog.csdnimg.cn/57ba39eb26b64e2396fbc4947b81526e.gif#pic_center)

# 引入方式
## gradle导入
1. 在更目录中的build.gradle添加 JitPack仓库

	```powershell
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
	```
	以上是官方提供的添加仓库方法，实测在较新的gradle版本中此方式无法成功生效，而是需要在settings.gradle中添加：
	

	```bash
	dependencyResolutionManagement {
    	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    	repositories {
        	...
        	maven { url 'https://jitpack.io' }
   	 }
	}
	```

2. 添加依赖
 

	```bash
	dependencies {
	    implementation 'com.github.EnthuDai:SimpleChart-Kotlin:1.0.8'
	}	
	```
 PS: 发布一个kotlin的android library到jitpack的过程极为艰难，尤其是在使用较新版本的gradle时，大家如果遇到问题，可以参考一下这篇文章 

> [kotlin android library 发布至jitpack问题及解决方式记载](https://blog.csdn.net/qq_28504151/article/details/122472628)

## 源码导入
1. 下载该项目源码

    ```bash
	git clone https://github.com/EnthuDai/SimpleChart-Kotlin.git
    ```
2. 在需要使用该库的项目中通过文件导入此模块
	![导入模块步骤1](https://img-blog.csdnimg.cn/d00e409f0d9b4f6a8b7900fa1f8f9d94.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBARW50aHXkuLY=,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
![导入模块步骤二，选择上一步中下载项目的chart文件夹](https://img-blog.csdnimg.cn/74ac54a65703425db87eede8fda2422e.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBARW50aHXkuLY=,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)

3. 在app的build.gradle的依赖中添加此库
	

	```powershell
	dependencies {

		...
	
   	 implementation project(':chart')
	}
	```

# 使用方式
## 布局文件

```xml
<com.poemdistance.chart.BarChart
        android:id="@+id/barChart"
        android:layout_width="0dp"
        android:layout_height="300dp"
        app:title="柱状图"
        app:xAxis="staDate"
        app:yAxis="profit"
        app:yAxisDesc="入账收益"/>
```

## 设置数据
数据设置的方式非常简单，支持直接传入JSON数组字符串，或传入List< Any>形式的对象集合。所有类型图表均支持这两种方式的数据传入。JSON数组字符串的导入代码如下：
```kotlin
val jsonData = """
        [{"staDate":"01-09","profit":235,"activeOrderCount":36},
        {"staDate":"01-08","profit":244,"activeOrderCount":33},
        {"staDate":"01-07","profit":159,"activeOrderCount":29},
        {"staDate":"01-06","profit":452,"activeOrderCount":48},
        {"staDate":"01-05","profit":116,"activeOrderCount":19},
        {"staDate":"01-04","profit":131,"activeOrderCount":22},
        {"staDate":"01-03","profit":345,"activeOrderCount":32},
        {"staDate":"01-02","profit":277,"activeOrderCount":35},
        {"staDate":"01-01","profit":206,"activeOrderCount":28}]
    """.trimIndent()
barChart.updateData(jsonData) // barChart为该图表的实例
```
## 事件监听
监听用户当前触摸的数据项

```kotlin
// 监听触摸事件，将获得Json字符串形式的当前触控数据，当触控点从图表移开时返回null
barChart.listener =  {
	it?.let{
		Log.d(LOG_TAG, it)
	}
}
```

# 配置项
## 所有图表均具备的配置项
|    属性名    |类型                              |默认值           			  |含义
|----------------|-------------------------------|-----------------------------|-----------------------------|
|title              |String                           |                                   |图表标题
|animation    |Boolean                       | True             |是否开启加载动画
|animationDuration|Int|500|加载动画时长
|showLegend|Boolean|自动（单维度数据时不显示）|是否显示图例


## 柱状图 com.poemdistance.chart.BarChart
|    属性名    |类型                              |默认值           			  |含义                           |是否必填
|----------------|-------------------------------|-----------------------------|-----------------------------|-----------------------------|
|xAxis|String|                                   |x轴数据的参数名|是
|yAxis|String| |y轴数据的参数名|是
|yAxisDesc|String|yAxis值|描述y轴数据项的参数名|否

## 折线图 com.poemdistance.chart.LineChart
|    属性名    |类型                              |默认值           			  |含义                           |是否必填
|----------------|-------------------------------|-----------------------------|-----------------------------|-----------------------------|
|xAxis|String|                                   |x轴数据的参数名|是
|yAxis|String| |y轴数据的参数名，支持多个，用“，”分隔|是
|yAxisDesc|String|yAxis值|描述y轴数据项的参数名，支持多个，用“，”分隔|否

## 饼图 com.poemdistance.chart.PieChart
|    属性名    |类型                              |默认值           			  |含义                           |是否必填
|----------------|-------------------------------|-----------------------------|-----------------------------|-----------------------------|
|name|String|                                   |数据项的展示参数名|是
|value|String| |数据项的值参数名|是

