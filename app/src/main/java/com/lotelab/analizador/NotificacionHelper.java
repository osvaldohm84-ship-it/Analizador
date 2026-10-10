package com.lotelab.analizador;
import android.app.*;import android.content.*;import android.os.*;import androidx.core.app.NotificationCompat;import java.util.*;
public class NotificacionHelper{
public static final String CANAL_ID="cumplimiento";
public static final String CANAL_NOMBRE="Cumplimiento";
public static final int NOTIF_ID=1001;
public static void crearCanal(Activity act){
if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
NotificationManager nm=(NotificationManager)act.getSystemService(Context.NOTIFICATION_SERVICE);
if(nm.getNotificationChannel(CANAL_ID)==null){
NotificationChannel canal=new NotificationChannel(CANAL_ID,CANAL_NOMBRE,NotificationManager.IMPORTANCE_DEFAULT);
canal.setDescription("Alertas de cambios de estado en los metodos");
nm.createNotificationChannel(canal);
}
}
}
public static void pedirPermisoSiNecesario(Activity act){
if(Build.VERSION.SDK_INT>=33){
if(act.checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=PackageManager.PERMISSION_GRANTED){
act.requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"},100);
}
}
}
public static void mostrarNotificacion(Activity act,String titulo,String texto){
try{
NotificationManager nm=(NotificationManager)act.getSystemService(Context.NOTIFICATION_SERVICE);
Intent intent=new Intent(act,MetodosActivity.class);
intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
PendingIntent pi=PendingIntent.getActivity(act,0,intent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
NotificationCompat.Builder b=new NotificationCompat.Builder(act,CANAL_ID)
.setSmallIcon(android.R.drawable.ic_dialog_info)
.setContentTitle(titulo)
.setContentText(texto)
.setStyle(new NotificationCompat.BigTextStyle().bigText(texto))
.setPriority(NotificationCompat.PRIORITY_DEFAULT)
.setAutoCancel(true)
.setContentIntent(pi);
nm.notify(NOTIF_ID,b.build());
}catch(Exception e){}
}
public static void agruparYMostrar(Activity act,List<Alerta> alertas){
if(alertas==null||alertas.isEmpty())return;
int total=alertas.size();
String titulo="🔔 "+total+" cambio"+(total==1?"":"s")+" en metodos";
StringBuilder cuerpo=new StringBuilder();
int mostrados=0;
for(Alerta a:alertas){
if(mostrados>=3)break;
cuerpo.append(a.getEmoji()).append(" ").append(a.nombreMetodo).append("\n");
mostrados++;
}
if(total>3)cuerpo.append("... y ").append(total-3).append(" mas");
mostrarNotificacion(act,titulo,cuerpo.toString());
}
public static void cancelar(Activity act){
try{
NotificationManager nm=(NotificationManager)act.getSystemService(Context.NOTIFICATION_SERVICE);
nm.cancel(NOTIF_ID);
}catch(Exception e){}
}
}
