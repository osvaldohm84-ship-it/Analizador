package com.lotelab.analizador;
import android.database.*;import android.database.sqlite.*;import java.util.*;
public class CumplimientoEngine{
static final String TABLA="cumplimiento_bitmask";
public static CumplimientoResultado calcular(SQLiteDatabase db,Set<Integer> dias,String categoria,String turno,double umbral){
CumplimientoResultado r=new CumplimientoResultado();
r.diasSeleccionados=new TreeSet<>(dias);
r.categoria=categoria;
r.turno=turno;
r.umbral=umbral;
r.totalNumerosCategoria=getTotalNumeros(categoria);
long maskDias=0L;
for(int d:dias){if(d>=1&&d<=31)maskDias|=(1L<<(d-1));}
if(maskDias==0){
r.advertencias.add("No hay días seleccionados");
return r;
}
Map<Integer,Map<String,Long>> datosPorValor=leerBitmasks(db,categoria,turno);
if(datosPorValor.isEmpty()){
r.advertencias.add("Bitmask vacía. Reconstruir primero.");
return r;
}
Set<String> mesesSet=new TreeSet<>();
for(Map<String,Long> m:datosPorValor.values()){
mesesSet.addAll(m.keySet());
}
r.mesesTotales=mesesSet.size();
if(r.mesesTotales==0){
r.advertencias.add("No hay meses en la bitmask");
return r;
}
for(int valor=0;valor<r.totalNumerosCategoria;valor++){
Map<String,Long> meses=datosPorValor.get(valor);
if(meses==null)continue;
int cumplidos=0;
for(String key:mesesSet){
Long bm=meses.get(key);
if(bm!=null&&(bm&maskDias)!=0)cumplidos++;
}
double tasa=(double)cumplidos/r.mesesTotales;
if(tasa>=umbral)r.pool.add(valor);
}
calcularMetricasPool(r,datosPorValor,mesesSet,maskDias);
calcularRachas(r,datosPorValor,mesesSet,maskDias);
calcularPorAnio(r,datosPorValor,mesesSet,maskDias);
generarAdvertencias(r);
calcularVeredicto(r);
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
static void calcularMetricasPool(CumplimientoResultado r,Map<Integer,Map<String,Long>> datos,Set<String> meses,long maskDias){
int mesesCumplidosPool=0;
int aciertos=0;
for(String mesKey:meses){
boolean cumplioEsteMes=false;
for(int valor:r.pool){
Map<String,Long> mapa=datos.get(valor);
if(mapa==null)continue;
Long bm=mapa.get(mesKey);
if(bm!=null&&(bm&maskDias)!=0){
cumplioEsteMes=true;
aciertos+=Long.bitCount(bm&maskDias);
}
}
if(cumplioEsteMes)mesesCumplidosPool++;
}
r.mesesCumplidos=mesesCumplidosPool;
r.tasaCumplimiento=(double)mesesCumplidosPool/r.mesesTotales;
r.aciertosObservados=aciertos;
int sorteosVentana=r.diasSeleccionados.size();
double probPorSorteo=(double)r.pool.size()/r.totalNumerosCategoria;
double probNoEnSorteo=1-probPorSorteo;
double probNoEnVentana=Math.pow(probNoEnSorteo,sorteosVentana);
double probAlMenosUno=1-probNoEnVentana;
r.aciertosEsperados=r.mesesTotales*probAlMenosUno;
}
static void calcularRachas(CumplimientoResultado r,Map<Integer,Map<String,Long>> datos,Set<String> meses,long maskDias){
List<Boolean> cumplidosPorMes=new ArrayList<>();
for(String mesKey:meses){
boolean cumplio=false;
for(int valor:r.pool){
Map<String,Long> mapa=datos.get(valor);
if(mapa==null)continue;
Long bm=mapa.get(mesKey);
if(bm!=null&&(bm&maskDias)!=0){cumplio=true;break;}
}
cumplidosPorMes.add(cumplio);
}
int rachaActual=0;
for(int i=cumplidosPorMes.size()-1;i>=0;i--){
if(cumplidosPorMes.get(i))rachaActual++;
else break;
}
r.rachaActual=rachaActual;
int sequiaMax=0;int sequiaActual=0;
for(int i=0;i<cumplidosPorMes.size();i++){
if(!cumplidosPorMes.get(i)){
sequiaActual++;
if(sequiaActual>sequiaMax)sequiaMax=sequiaActual;
}else{
sequiaActual=0;
}
}
r.mayorSequia=sequiaMax;
r.mesesConsecutivosSinCumplir=sequiaActual;
}
static void calcularPorAnio(CumplimientoResultado r,Map<Integer,Map<String,Long>> datos,Set<String> meses,long maskDias){
Map<Integer,List<Boolean>> porAnio=new TreeMap<>();
for(String mesKey:meses){
String[] partes=mesKey.split("_");
int anio=Integer.parseInt(partes[0]);
boolean cumplio=false;
for(int valor:r.pool){
Map<String,Long> mapa=datos.get(valor);
if(mapa==null)continue;
Long bm=mapa.get(mesKey);
if(bm!=null&&(bm&maskDias)!=0){cumplio=true;break;}
}
List<Boolean> lista=porAnio.get(anio);
if(lista==null){lista=new ArrayList<>();porAnio.put(anio,lista);}
lista.add(cumplio);
}
for(Map.Entry<Integer,List<Boolean>> e:porAnio.entrySet()){
int cumplidos=0;
for(boolean b:e.getValue())if(b)cumplidos++;
r.porAnio.put(e.getKey(),new int[]{cumplidos,e.getValue().size()});
}
}
static void generarAdvertencias(CumplimientoResultado r){
if(r.pool.isEmpty()){
r.advertencias.add("Ningún número cumple el umbral "+(int)(r.umbral*100)+"%");
return;
}
if(r.pool.size()>20){
r.advertencias.add("Pool grande ("+r.pool.size()+" números). Útil como filtro.");
}
int historialAnios=r.porAnio.size();
if(historialAnios<5){
r.advertencias.add("Historial corto ("+historialAnios+" años). Validación limitada.");
}
if(!sonContiguos(r.diasSeleccionados)){
r.advertencias.add("Días dispersos. Puede haber falsos positivos.");
}
int nDias=r.diasSeleccionados.size();
if(nDias>10){
r.advertencias.add("Ventana amplia ("+nDias+" días). Trivial por tamaño.");
}
}
static void calcularVeredicto(CumplimientoResultado r){
if(r.pool.isEmpty()){r.veredicto="SIN_DATOS";return;}
if(r.mesesTotales==0){r.veredicto="SIN_DATOS";return;}
double zAprox=(r.aciertosObservados-r.aciertosEsperados)/Math.sqrt(Math.max(1,r.aciertosEsperados));
if(zAprox>=3.0)r.veredicto="FUERTE";
else if(zAprox>=2.0)r.veredicto="POSIBLE";
else if(zAprox>=1.0)r.veredicto="DEBIL";
else r.veredicto="RUIDO";
}
static boolean sonContiguos(Set<Integer> dias){
List<Integer> sorted=new ArrayList<>(dias);
Collections.sort(sorted);
for(int i=1;i<sorted.size();i++){
if(sorted.get(i)-sorted.get(i-1)!=1)return false;
}
return true;
}
static int getTotalNumeros(String categoria){
if("CENTENA".equals(categoria)||"DECENA".equals(categoria)||"TERMINAL".equals(categoria))return 10;
return 100;
}
public static String resumenCorto(CumplimientoResultado r){
StringBuilder s=new StringBuilder();
s.append("Categoría: ").append(r.categoria).append("\n");
s.append("Turno: ").append(r.turno).append("\n");
s.append("Días: ").append(r.diasSeleccionados.toString()).append("\n");
s.append("Umbral: ").append((int)(r.umbral*100)).append("%\n\n");
s.append("Pool (").append(r.pool.size()).append("): ").append(r.getPoolComoTexto()).append("\n\n");
s.append("Meses: ").append(r.mesesCumplidos).append("/").append(r.mesesTotales);
s.append(" (").append(String.format("%.1f",r.tasaCumplimiento*100)).append("%)\n");
s.append("Aciertos: ").append(r.aciertosObservados);
s.append(" (esperado: ").append(String.format("%.0f",r.aciertosEsperados)).append(")\n");
s.append("Racha actual: ").append(r.rachaActual).append(" meses\n");
s.append("Mayor sequía: ").append(r.mayorSequia).append(" meses\n");
s.append("Veredicto: ").append(r.veredicto).append("\n");
if(!r.advertencias.isEmpty()){
s.append("\nAdvertencias:\n");
for(String a:r.advertencias)s.append("• ").append(a).append("\n");
}
return s.toString();
}
}
