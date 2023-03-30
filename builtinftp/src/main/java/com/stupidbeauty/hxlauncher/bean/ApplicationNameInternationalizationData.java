package com.stupidbeauty.hxlauncher.bean;

import java.util.List;

public class ApplicationNameInternationalizationData
{
    private List<ApplicationNamePair> applicationNames; //!<名字列表。

    public List<ApplicationNamePair> getApplicationNames() {
        return applicationNames;
    }

    /**
     * 获取国际化名字。
     * @param holderpackageName 包名。
     * @return 对应的国际化名字。
     */
    public String getInternationalizationName(String holderpackageName)
    {
        String result=null; //结果。

        for (ApplicationNamePair currentPackage:applicationNames) //一个个包地检查。
        {
            if (currentPackage.getPackageName().equals(holderpackageName)) //包名匹配。
            {
                result=currentPackage.getReadableApplicationName(); //获取国际化名字。

                break; //跳出。
            } //if (currentPackage.getPackageName().equals(holderpackageName)) //包名匹配。
        } //for (ApplicationNamePair currentPackage:applicationNames) //一个个包地检查。

        return  result;
    } //public String getInternationalizationName(String holderpackageName)
}

