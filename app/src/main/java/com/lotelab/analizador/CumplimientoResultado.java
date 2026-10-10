package com.lotelab.analizador;
import java.util.*;
public class CumplimientoResultado{
public List<Integer> pool=new ArrayList<>();
public int totalNumerosCategoria=100;
public int mesesTotales=0;
public int mesesCumplidos=0;
public double tasaCumplimiento=0;
public int aciertosObservados=0;
public double aciertosEsperados=0;
public int rachaActual=0;
public int mayorSequia=0;
public int mesesConsecutivosSinCumplir=0;
public Map<Integer,int[]> porAnio=new TreeMap<>();
public List<String> advertencias=new ArrayList<>();
public String categoria="FIJO";
public String turno="AMBOS";
public double umbral=0.95;
public Set<Integer> diasSeleccionados=new TreeSet<>();
public String fechaInicio="";
public String fechaFin="";
public String veredicto="SIN_DATOS";
public String getResumen(){
StringBuilder s=new StringBuilder();
s.append("Pool: ").append(pool.size()).append(" números");
s.append(" | Tasa: ").append(String.format("%.1f",tasaCumplimiento*100)).append("%");
s.append(" | Meses: ").append(mesesCumplidos).append("/").append(mesesTotales);
s.append(" | Aciertos: ").append(aciertosObservados);
s.append(" | Racha: ").append(rachaActual);
s.append(" | Sequía: ").append(mayorSequia);
return s.toString();
}
public String getPoolComoTexto(){
if(pool.isEmpty())return "(vacío)";
StringBuilder s=new StringBuilder();
for(int i=0;i<pool.size();i++){
if("CENTENA".equals(categoria)||"DECENA".equals(categoria)||"TERMINAL".equals(categoria)){
s.append(pool.get(i));
}else{
s.append(String.format("%02d",pool.get(i)));
}
if(i<pool.size()-1)s.append(", ");
}
return s.toString();
}
}
