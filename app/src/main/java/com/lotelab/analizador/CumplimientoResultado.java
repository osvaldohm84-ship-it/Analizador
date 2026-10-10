package com.lotelab.analizador;
import java.util.*;
public class CumplimientoResultado{
public static class PoolN{
public int tamano=0;
public List<Integer> numeros=new ArrayList<>();
public int mesesCumplidos=0;
public int mesesFallados=0;
public int rachaActual=0;
public int mayorRacha=0;
public List<String> mesesFalladosLista=new ArrayList<>();
public String ultimoFallo="";
}
public String categoria="FIJO";
public String turno="AMBOS";
public Set<Integer> diasSeleccionados=new TreeSet<>();
public int mesesTotales=0;
public List<String> todosLosMeses=new ArrayList<>();
public Map<Integer,Integer> frecuenciaPorNumero=new TreeMap<>();
public Map<Integer,List<Integer>> numerosPorMes=new TreeMap<>();
public List<PoolN> pools=new ArrayList<>();
public String getResumenPool(PoolN p){
StringBuilder s=new StringBuilder();
s.append("Top ").append(p.tamano).append(": ");
s.append(getPoolComoTexto(p.numeros)).append("\n");
s.append("  Cumplimiento: ").append(p.mesesCumplidos).append("/").append(mesesTotales);
s.append(" (").append(String.format("%.1f",100.0*p.mesesCumplidos/mesesTotales)).append("%)\n");
s.append("  Fallos: ").append(p.mesesFallados).append("\n");
s.append("  Racha actual: ").append(p.rachaActual).append(" meses\n");
s.append("  Mayor racha: ").append(p.mayorRacha).append(" meses\n");
if(!p.ultimoFallo.isEmpty()){
s.append("  Último fallo: ").append(p.ultimoFallo).append("\n");
}
return s.toString();
}
public String getPoolComoTexto(List<Integer> lista){
if(lista.isEmpty())return "(vacío)";
StringBuilder s=new StringBuilder();
for(int i=0;i<lista.size();i++){
if("CENTENA".equals(categoria)||"DECENA".equals(categoria)||"TERMINAL".equals(categoria)){
s.append(lista.get(i));
}else{
s.append(String.format("%02d",lista.get(i)));
}
if(i<lista.size()-1)s.append(", ");
}
return s.toString();
}
}
