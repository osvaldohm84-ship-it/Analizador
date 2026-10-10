package com.lotelab.analizador;
import android.content.*;import android.database.*;import android.database.sqlite.*;import java.util.*;
public class MetodoManager{
public static final String TABLA="metodos";
public static final String TABLA_HIST="metodos_historial";
public static long guardar(Context ctx,Metodo m){
SQLiteOpenHelper dbh=new DBHelper(ctx);
SQLiteDatabase db=dbh.getWritableDatabase();
ContentValues v=new ContentValues();
v.put("nombre",m.nombre);
v.put("categoria",m.categoria);
v.put("turno",m.turno);
v.put("dias_bitmask",m.diasBitmask);
v.put("tamano_pool",m.tamanoPool);
v.put("numeros_pool",m.numerosPoolCsv);
v.put("fecha_creacion",System.currentTimeMillis());
v.put("estado",m.estado);
v.put("racha_actual",m.rachaActual);
v.put("mayor_racha",m.mayorRacha);
v.put("ultimo_fallo",m.ultimoFallo);
v.put("meses_consecutivos_fallando",m.mesesConsecutivosFallando);
v.put("total_meses",m.totalMeses);
v.put("total_cumplidos",m.totalCumplidos);
v.put("total_fallados",m.totalFallados);
v.put("activo",1);
long id=db.insert(TABLA,null,v);
m.id=id;
db.close();
return id;
}
public static List<Metodo> listar(Context ctx){
List<Metodo> lista=new ArrayList<>();
SQLiteOpenHelper dbh=new DBHelper(ctx);
SQLiteDatabase db=dbh.getReadableDatabase();
Cursor c=db.rawQuery("SELECT id,nombre,categoria,turno,dias_bitmask,tamano_pool,numeros_pool,fecha_creacion,estado,racha_actual,mayor_racha,ultimo_fallo,meses_consecutivos_fallando,total_meses,total_cumplidos,total_fallados,activo FROM "+TABLA+" WHERE activo=1 ORDER BY id DESC",null);
while(c.moveToNext()){
Metodo m=new Metodo();
m.id=c.getLong(0);
m.nombre=c.getString(1);
m.categoria=c.getString(2);
m.turno=c.getString(3);
m.diasBitmask=c.getInt(4);
m.tamanoPool=c.getInt(5);
m.numerosPoolCsv=c.getString(6);
m.fechaCreacion=c.getLong(7);
m.estado=c.getString(8);
m.rachaActual=c.getInt(9);
m.mayorRacha=c.getInt(10);
m.ultimoFallo=c.getString(11);
m.mesesConsecutivosFallando=c.getInt(12);
m.totalMeses=c.getInt(13);
m.totalCumplidos=c.getInt(14);
m.totalFallados=c.getInt(15);
m.activo=c.getInt(16);
lista.add(m);
}
c.close();
db.close();
return lista;
}
public static int contar(Context ctx){
SQLiteOpenHelper dbh=new DBHelper(ctx);
SQLiteDatabase db=dbh.getReadableDatabase();
Cursor c=db.rawQuery("SELECT COUNT(*) FROM "+TABLA+" WHERE activo=1",null);
c.moveToFirst();
int n=c.getInt(0);
c.close();
db.close();
return n;
}
public static void eliminar(Context ctx,long id){
SQLiteOpenHelper dbh=new DBHelper(ctx);
SQLiteDatabase db=dbh.getWritableDatabase();
db.delete(TABLA,"id=?",new String[]{String.valueOf(id)});
db.delete(TABLA_HIST,"metodo_id=?",new String[]{String.valueOf(id)});
db.close();
}
public static void actualizarEstado(Context ctx,long id,String estado,int racha,int rachaMayor,String ultimoFallo,int mesesFallando,int totalMeses,int totalCumplidos,int totalFallados){
SQLiteOpenHelper dbh=new DBHelper(ctx);
SQLiteDatabase db=dbh.getWritableDatabase();
ContentValues v=new ContentValues();
v.put("estado",estado);
v.put("racha_actual",racha);
v.put("mayor_racha",rachaMayor);
v.put("ultimo_fallo",ultimoFallo);
v.put("meses_consecutivos_fallando",mesesFallando);
v.put("total_meses",totalMeses);
v.put("total_cumplidos",totalCumplidos);
v.put("total_fallados",totalFallados);
db.update(TABLA,v,"id=?",new String[]{String.valueOf(id)});
db.close();
}
static class DBHelper extends SQLiteOpenHelper{
DBHelper(Context c){super(c,"loteria.db",null,6);}
public void onCreate(SQLiteDatabase d){}
public void onUpgrade(SQLiteDatabase d,int o,int n){}
}
}
