package com.lotelab.analizador;
import android.database.*;import android.database.sqlite.*;import java.util.*;
public class CumplimientoEngine{
static final String TABLA="cumplimiento_bitmask";
public static final int[] TAMANOS_POOL={10,15,20,25,30};
public static CumplimientoResultado calcular(SQLiteDatabase db,Set<Integer> dias,String categoria,String turno){
CumplimientoResultado r=new CumplimientoResultado();
r.diasSeleccionados=new TreeSet<>(dias);
r.categoria=categoria;
r.turno=turno;
long maskDias=0L;
for(int d:dias){if(d>=1&&d<=31)maskDias|=(1L<<(d-1));}
if(maskDias==0)return r;
int totalNumeros=getTotalNumeros(categoria);
Map<Integer,Map<String,Long>> datos=leerBitmasks(db,categoria,turno);
if(datos.isEmpty())return r;
Set<String> mesesSet=new TreeSet<>();
for(Map<String,Long> m:datos.values())mesesSet.addAll(m.keySet());
r.mesesTotales=mesesSet.size();
r.todosLosMeses=new ArrayList<>(mesesSet);
if(r.mesesTotales==0)return r;
Map<String,List<Integer>> numerosPorMes=new TreeMap<>();
for(String mes:mesesSet){
List<Integer> numerosDelMes=new ArrayList<>();
for(int valor=0;valor<totalNumeros;valor++){
Map<String,Long> meses=datos.get(valor);
if(meses==null)continue;
Long bm=meses.get(mes);
if(bm!=null&&(bm&maskDias)!=0)numerosDelMes.add(valor);
}
numerosPorMes.put(mes,numerosDelMes);
}
r.numerosPorMes=numerosPorMes;
Map<Integer,Integer> frec=new TreeMap<>();
for(int valor=0;valor<totalNumeros;valor++){
int cuenta=0;
for(List<Integer> lista:numerosPorMes.values()){
if(lista.contains(valor))cuenta++;
}
if(cuenta>0)frec.put(valor,cuenta);
}
r.frecuenciaPorNumero=frec;
List<Map.Entry<Integer,Integer>> ordenados=new ArrayList<>(frec.entrySet());
Collections.sort(ordenados,(a,b)->b.getValue()-a.getValue());
List<Integer> ranking=new ArrayList<>();
for(Map.Entry<Integer,Integer> e:ordenados)ranking.add(e.getKey());
for(int tamano:TAMANOS_POOL){
if(tamano>ranking.size())continue;
CumplimientoResultado.PoolN p=new CumplimientoResultado.PoolN();
p.tamano=tamano;
for(int i=0;i<tamano;i++)p.numeros.add(ranking.get(i));
Set<Integer> setPool=new HashSet<>(p.numeros);
List<Boolean> cumplidos=new ArrayList<>();
List<String> fallados=new ArrayList<>();
for(String mes:mesesSet){
List<Integer> numsMes=numerosPorMes.get(mes);
boolean cumple=false;
if(numsMes!=null){
for(int n:numsMes){
if(setPool.contains(n)){cumple=true;break;}
}
}
cumplidos.add(cumple);
if(!cumple)fallados.add(mes);
}
p.mesesCumplidos=0;
for(boolean b:cumplidos)if(b)p.mesesCumplidos++;
p.mesesFallados=fallados.size();
p.mesesFalladosLista=fallados;
if(!fallados.isEmpty())p.ultimoFallo=fallados.get(fallados.size()-1);
int racha=0;
for(int i=cumplidos.size()-1;i>=0;i--){
if(cumplidos.get(i))racha++;
else break;
}
p.rachaActual=racha;
int mayor=0;int actual=0;
for(boolean b:cumplidos){
if(b){actual++;if(actual>mayor)mayor=actual;}
else actual=0;
}
p.mayorRacha=mayor;
r.pools.add(p);
}
return r;
}
static Map<Integer,Map<String,Long>> leerBitmasks(SQLiteDatabase db,String categoria,String turno){
Map<Integer,Map<String,Long>> resultado=new HashMap<>();
try{
Cursor c=db.rawQuery("SELECT valor,anio,mes,bitmask FROM "+TABLA+" WHERE categoria=? AND turno=?",new String[]{categoria,turno});
while(c.moveToNext()){
int valor=c.getInt(0);
int anio=c.getInt(1);
int mes=c.getInt(2);
long bitmask=((long)c.getInt(3))&0xFFFFFFFFL;
String key=anio+"_"+mes;
Map<String,Long> mapa=resultado.get(valor);
if(mapa==null){mapa=new HashMap<>();resultado.put(valor,mapa);}
mapa.put(key,bitmask);
}
c.close();
}catch(Exception e){}
return resultado;
}
static int getTotalNumeros(String categoria){
if("CENTENA".equals(categoria)||"DECENA".equals(categoria)||"TERMINAL".equals(categoria))return 10;
return 100;
}
public static String resumenCorto(CumplimientoResultado r){
StringBuilder s=new StringBuilder();
s.append("Categoria: ").append(r.categoria).append("\n");
s.append("Turno: ").append(r.turno).append("\n");
s.append("Dias: ").append(r.diasSeleccionados.toString()).append("\n");
s.append("Meses analizados: ").append(r.mesesTotales).append("\n\n");
if(r.pools.isEmpty()){
s.append("Sin datos suficientes\n");
return s.toString();
}
for(CumplimientoResultado.PoolN p:r.pools){
s.append(r.getResumenPool(p)).append("\n");
}
return s.toString();
}
}
