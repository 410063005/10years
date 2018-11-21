如何判断Android应用的notification是否打开？

2012年时没有方法可以用于判断notification是否打开。[Google I/O 2012 video](https://youtu.be/Yc8YrVc47TI?t=48m4s)中有提到。

2016年开始官方提供[NotificationManagerCompat.areNotificationsEnabled()](https://developer.android.com/reference/android/support/v4/app/NotificationManagerCompat.html#areNotificationsEnabled())用于判断notification是否打开 (support库24.0中添加)。 [Google I/O 2016 video](https://youtu.be/w45y_w4skKs?list=PLOU2XLYxmsILe6_eGvDN3GyiodoV3qNSC&t=192)中有提到。

[这里](https://stackoverflow.com/questions/11649151/android-4-1-how-to-check-notifications-are-disabled-for-the-application)提供了另一个办法(非官方)：

```java
/**
 * Created by desgraci on 5/7/15.
*/
public class NotificationsUtils {

    private static final String CHECK_OP_NO_THROW = "checkOpNoThrow";
    private static final String OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION";

    public static boolean isNotificationEnabled(Context context) {

        AppOpsManager mAppOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);

        ApplicationInfo appInfo = context.getApplicationInfo();

        String pkg = context.getApplicationContext().getPackageName();

        int uid = appInfo.uid;

        Class appOpsClass = null; /* Context.APP_OPS_MANAGER */

        try {

            appOpsClass = Class.forName(AppOpsManager.class.getName());

            Method checkOpNoThrowMethod = appOpsClass.getMethod(CHECK_OP_NO_THROW, Integer.TYPE, Integer.TYPE, String.class);

            Field opPostNotificationValue = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION);
            int value = (int)opPostNotificationValue.get(Integer.class);

            return ((int)checkOpNoThrowMethod.invoke(mAppOps,value, uid, pkg) == AppOpsManager.MODE_ALLOWED);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }
}
```

# 参考

[ref](https://stackoverflow.com/questions/11649151/android-4-1-how-to-check-notifications-are-disabled-for-the-application)

[ref](https://stackoverflow.com/questions/27815782/how-to-check-if-my-app-is-allowed-to-show-notification)