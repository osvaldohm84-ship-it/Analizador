package com.lotelab.analizador;
import android.content.*;import android.database.*;import android.database.sqlite.*;import java.util.*;
public class AlertaManager{
public static final String TABLA="alertas";
public static long guardar(Context ctx,Alerta a){
SQLiteOpenHelper dbh=new DBHelper(ctx);
SQLiteDatabase db=dbh.getWritableDatabase();
ContentValues v=new ContentValues();
v.put("metodo_id",a.metodoId);
v.put("nombre_metodo",a.nombreMetodo);
v.put("tipo",a.tipo);
v.put("estado_anterior",a.estadoAnterior);
v.put("estado_nuevo",a.estadoNuevo);
v.put("mensaje",a.mensaje);
v.put("fecha",System.currentTimeMillis());
v.put("leida",0);
long id=db.insert(TABLA,null,v);
db.close();
return id;
}
public static List<Alerta> listarNoLeidas(Context ctx){
return listar(ctx,"leida=0");
}
public static List<Alerta> listarTodas(Context ctx){
return listar(ctx,"1=1");
}
static List<Alerta> listar(Context ctx,String where){
List<Alerta> lista=new ArrayList<>();
SQLiteOpenHelper dbh=new DBHelper(ctx);
SQLiteDatabase db=dbh.getReadableDatabase();
Cursor c=db.rawQuery("SELECT id,metodo_id,nombre_metodo,tipo,estado_anterior,estado_nuevo,mensaje,fecha,leida FROM "+TABLA+" WHERE "+where+" ORDER BY fecha DESC LIMIT 50",null);
while(c.moveToNext()){
Alerta a=new Alerta();
a.id=c.getLong(0);
a.metodoId=c.getLong(1);
a.nombreMetodo=c.getString(2);
a.tipo=c.getString(3);
a.estadoAnterior=c.getString(4);
a.estadoNuevo=c.getString(5);
a.mensaje=c.getString(6);
a.fecha=c.getLong(7);
a.leida=c.getInt(8);
lista.add(a);
}
c.close();
db.close();
return lista;
}
public static int contarNoLeidas(Context ctx){
SQLiteOpenHelper dbh=new DBHelper(ctx);
SQLiteDatabase db=dbh.getReadableDatabase();
Cursor c=db.rawQuery("SELECT COUNT(*) FROM "+TABLA+" WHERE leida=0",null);
c.moveToFirst();
int n=c.getInt(0);
c.close();
db.close();
return n;
}
public static void marcarComoLeida(Context ctx,long id){
SQLiteOpenHelper dbh=new DBHelper(ctx);
SQLiteDatabase db=dbh.getWritableDatabase();
ContentValues v=new ContentValues();
v.put("leida",1);
db.update(TABLA,v,"id=?",new String[]{String.valueOf(id)});
db.close();
}
public static void marcarTodasComoLeidas(Context ctx){
SQLiteOpenHelper dbh=new DBHelper(ctx);
SQLiteDatabase db=dbh.getWritableDatabase();
ContentValues v=new ContentValues();
v.put("leida",1);
db.update(TABLA,v,"leida=0",null);
db.close();
}
public static void eliminar(Context ctx,long id){
SQLiteOpenHelper dbh=new DBHelper(ctx);
SQLiteDatabase db=dbh.getWritableDatabase();
db.delete(TABLA,"id=?",new String[]{String.valueOf(id)});
db.close();
}
public static void limpiar(Context ctx){
SQLiteOpenHelper dbh=new DBHelper(ctx);
SQLiteDatabase db=dbh.getWritableDatabase();
db.delete(TABLA,null,null);
db.close();
}
static class DBHelper extends SQLiteOpenHelper{
DBHelper(Context c){super(c,"loteria.db",null,6);}
public void onCreate(SQLiteDatabase d){}
public void onUpgrade(SQLiteDatabase d,int o,int n){}
}
}
