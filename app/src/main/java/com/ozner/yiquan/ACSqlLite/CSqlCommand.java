package com.ozner.yiquan.ACSqlLite;

import android.content.Context;

import com.ozner.yiquan.Command.NetCacheWork;
import com.ozner.yiquan.Command.UserDataPreference;

import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by C-sir@hotmail.com  on 2016/1/11.
 */
public class CSqlCommand  {
    private static class LazyHolder {
            private static final CSqlCommand INSTANCE = new CSqlCommand();
         }
      private CSqlCommand (){}
       public static final CSqlCommand getInstance() {
            return LazyHolder.INSTANCE;
       }
    public  void SetTableName(final Context context,String table)
   {
       String Sql = String.format("CREATE TABLE IF NOT EXISTS %s (cacheid integer PRIMARY KEY autoincrement,action Text,data TEXT,failcount integer,time default (datetime('now', 'localtime')))", table);
       CSqliteDb.execSQLNonQuery(context,Sql, new String[]{});
       UserDataPreference.SetUserData(context,UserDataPreference.NetCahceWorkTableName,table);
   }
    public List<NetCacheWork> GetNetCacheWorks(final Context context)
    {
        String tablename= UserDataPreference.GetUserData(context, UserDataPreference.NetCahceWorkTableName, "COZNERCACHE");
        if(context!=null&&tablename!=null)
        {
            String sql=String.format("select * from %s",tablename);
             List<String[]> result=  CSqliteDb.ExecSQL(context, sql, new String[]{});
            if(result!=null&&result.size()>0)
            {
                List<NetCacheWork> listnet=new LinkedList<NetCacheWork>();
                for (String[] row:result
                     ) {
                    try {
                        NetCacheWork netcache = new NetCacheWork();
                        netcache.id = Integer.parseInt(row[0]);
                        netcache.action = row[1];
                        netcache.data = row[2];
                        netcache.failcount=Integer.parseInt(row[3]);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        netcache.datetime = sdf.parse(row[4]);
                        listnet.add(netcache);
                    }catch (Exception ex)
                    {
                        continue;
                    }

                }
                return listnet;
            }
            return null;
        }else{
            return null;
        }
    }
    public  void  AddNetCacheWorks(final Context context,NetCacheWork netCacheWork)
    {
        String tablename= UserDataPreference.GetUserData(context, UserDataPreference.NetCahceWorkTableName, "COZNERCACHE");
        if(context!=null&&tablename!=null)
        {
            String sql=String.format("INSERT OR REPLACE INTO %s (action,failcount,data) VALUES (?,?,?)", tablename);
            CSqliteDb.execSQLNonQuery(context,sql,new Object[]{netCacheWork.action,netCacheWork.failcount,netCacheWork.data});
        }
    }
    public void UpdateNetCacheFaildCount(final Context context,NetCacheWork netCacheWork)
    {
        String tablename= UserDataPreference.GetUserData(context, UserDataPreference.NetCahceWorkTableName, "COZNERCACHE");
        if(context!=null&&tablename!=null)
        {
            String sql=String.format("UPDATE %s set failcount=? WHERE cacheid=? ", tablename);
            CSqliteDb.execSQLNonQuery(context,sql,new Object[]{netCacheWork.failcount,netCacheWork.id});
        }
    }
    public  void  RemoveNetCacheWorks(final Context context,NetCacheWork netCacheWork)
    {
        String tablename= UserDataPreference.GetUserData(context, UserDataPreference.NetCahceWorkTableName, "COZNERCACHE");
        if(context!=null&&tablename!=null) {
            String sql=String.format("delete from %s where cacheid=?", tablename);
            CSqliteDb.execSQLNonQuery(context,sql,new Object[]{netCacheWork.id});
        }

    }
    /**
     * 清楚所有任务
     * */
    public void ClearNetCacheWorks(final Context context)
    {
        List<NetCacheWork> netCacheWorks=GetNetCacheWorks(context);
        if(netCacheWorks!=null&&netCacheWorks.size()>0)
        {
            for(NetCacheWork netCacheWork:netCacheWorks)
            {
                RemoveNetCacheWorks(context,netCacheWork);
            }
        }
    }
}
