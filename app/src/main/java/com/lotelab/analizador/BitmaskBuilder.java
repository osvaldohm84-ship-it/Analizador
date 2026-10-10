package com.lotelab.analizador;
import android.database.*;import android.database.sqlite.*;import java.util.*;
public class BitmaskBuilder{
public static final String[] CATEGORIAS={"FIJO","C1","C2","CENTENA","DECENA","TERMINAL"};
public static final String[] TURNOS={"AMBOS","T","N"};
static final String TABLA="cumplimiento_bitmask";
public static void limpiarBitmask(SQLiteDatabase db){
try{db.execSQL("DELETE FROM "+TABLA);}catch(Exception e){}
}
public static int contarFilas(SQLiteDatabase db){
try{
Cursor c=db.rawQuery("SELECT COUNT(*) FROM "+TABLA,null);
c.moveToFirst();
int n=c.getInt(0);
c.close();
return n;
}catch(Exception e){return -1;}
}
public static int poblarBitmask(SQLiteDatabase db){
try{
db.beginTransaction();
db.execSQL("DELETE FROM "+TABLA);
Map<String,Integer> mapa=new HashMap<>();
Cursor c=db.rawQuery("SELECT date,tn,cent,fijo,c1,c2 FROM draws",null);
while(c.moveToNext()){
String date=c.getString(0);
String tn=c.getString(1);
String cent=c.getString(2);
String fijo=c.getString(3);
String c1=c.getString(4);
String c2=c.getString(5);
int[] fecha=parseFecha(date);
if(fecha==null)continue;
int anio=fecha[0];
int mes=fecha[1];
int dia=fecha[2];
int bitDia=1<<(dia-1);
int fijoVal=parseInt(fijo);
int c1Val=parseInt(c1);
int c2Val=parseInt(c2);
int centVal=parseInt(cent);
if(fijoVal>=0)acumular(mapa,"FIJO",fijoVal,anio,mes,bitDia,tn);
if(c1Val>=0)acumular(mapa,"C1",c1Val,anio,mes,bitDia,tn);
if(c2Val>=0)acumular(mapa,"C2",c2Val,anio,mes,bitDia,tn);
if(centVal>=0&&centVal<=9){
acumular(mapa,"CENTENA",centVal,anio,mes,bitDia,tn);
if(fijoVal>=0){
acumular(mapa,"DECENA",fijoVal/10,anio,mes,bitDia,tn);
acumular(mapa,"TERMINAL",fijoVal%10,anio,mes,bitDia,tn);
}
}
}
c.close();
SQLiteStatement ins=db.compileStatement("INSERT OR REPLACE INTO "+TABLA+"(categoria,turno,valor,anio,mes,bitmask,aciertos_count) VALUES(?,?,?,?,?,?,?)");
int contador=0;
for(Map.Entry<String,Integer> e:mapa.entrySet()){
String[] partes=e.getKey().split("_");
String categoria=partes[0];
int valor=Integer.parseInt(partes[1]);
int anio=Integer.parseInt(partes[2]);
int mes=Integer.parseInt(partes[3]);
String turno=partes[4];
int bitmask=e.getValue();
int aciertos=Integer.bitCount(bitmask);
ins.clearBindings();
ins.bindString(1,categoria);
ins.bindString(2,turno);
ins.bindLong(3,valor);
ins.bindLong(4,anio);
ins.bindLong(5,mes);
ins.bindLong(6,bitmask);
ins.bindLong(7,aciertos);
ins.executeInsert();
contador++;
}
db.setTransactionSuccessful();
db.endTransaction();
return contador;
}catch(Exception e){
try{db.endTransaction();}catch(Exception ex){}
return -1;
}
}
static void acumular(Map<String,Integer> mapa,String cat,int valor,int anio,int mes,int bitDia,String tn){
String base=cat+"_"+valor+"_"+anio+"_"+mes;
String kAmbos=base+"_AMBOS";
Integer v=mapa.get(kAmbos);
mapa.put(kAmbos,(v==null?0:v)|bitDia);
if("T".equals(tn)){
String kT=base+"_T";
Integer v2=mapa.get(kT);
mapa.put(kT,(v2==null?0:v2)|bitDia);
}else if("N".equals(tn)){
String kN=base+"_N";
Integer v3=mapa.get(kN);
mapa.put(kN,(v3==null?0:v3)|bitDia);
}
}
static int[] parseFecha(String f){
try{
String[] p=f.split("[-/]");
if(p.length<3)return null;
int dia=Integer.parseInt(p[0]);
int mes=Integer.parseInt(p[1]);
int anio=Integer.parseInt(p[2]);
if(dia<1||dia>31||mes<1||mes>12)return null;
return new int[]{anio,mes,dia};
}catch(Exception e){return null;}
}
static int parseInt(String s){
try{return Integer.parseInt(s);}catch(Exception e){return -1;}
}
}
