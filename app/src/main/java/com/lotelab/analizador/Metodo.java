package com.lotelab.analizador;
import java.util.*;
public class Metodo{
public long id=0;
public String nombre="";
public String categoria="FIJO";
public String turno="AMBOS";
public int diasBitmask=0;
public int tamanoPool=0;
public String numerosPoolCsv="";
public long fechaCreacion=0;
public String estado="NUEVO";
public int rachaActual=0;
public int mayorRacha=0;
public String ultimoFallo="";
public int mesesConsecutivosFallando=0;
public int totalMeses=0;
public int totalCumplidos=0;
public int totalFallados=0;
public int activo=1;
public List<Integer> getNumeros(){
List<Integer> lista=new ArrayList<>();
if(numerosPoolCsv==null||numerosPoolCsv.isEmpty())return lista;
String[] partes=numerosPoolCsv.split(",");
for(String p:partes){
try{lista.add(Integer.parseInt(p.trim()));}catch(Exception e){}
}
return lista;
}
public String getDiasComoTexto(){
StringBuilder sb=new StringBuilder();
for(int d=1;d<=31;d++){
if((diasBitmask&(1<<(d-1)))!=0){
if(sb.length()>0)sb.append(",");
sb.append(d);
}
}
return sb.toString();
}
public String getEstadoEmoji(){
if("CUMPLIENDO".equals(estado))return "🟢";
if("RIESGO".equals(estado))return "🟡";
if("ALERTA".equals(estado))return "🔴";
return "⚪";
}
public String getResumen(){
StringBuilder s=new StringBuilder();
s.append(getEstadoEmoji()).append(" ").append(nombre).append("\n");
s.append("Categoria: ").append(categoria).append(" | Turno: ").append(turno).append("\n");
s.append("Dias: ").append(getDiasComoTexto()).append("\n");
s.append("Pool: ").append(tamanoPool).append(" numeros\n");
s.append("Cumplimiento: ").append(totalCumplidos).append("/").append(totalMeses);
if(totalMeses>0)s.append(" (").append(String.format("%.1f",100.0*totalCumplidos/totalMeses)).append("%)");
s.append("\n");
s.append("Racha actual: ").append(rachaActual).append(" meses\n");
s.append("Mayor racha: ").append(mayorRacha).append(" meses\n");
if(!ultimoFallo.isEmpty())s.append("Ultimo fallo: ").append(ultimoFallo).append("\n");
return s.toString();
}
}
