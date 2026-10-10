package com.lotelab.analizador;
public class Alerta{
public static final String TIPO_RIESGO="RIESGO";
public static final String TIPO_ALERTA="ALERTA";
public static final String TIPO_RECUPERADO="RECUPERADO";
public static final String TIPO_NUEVO_FALLO="NUEVO_FALLO";
public long id=0;
public long metodoId=0;
public String nombreMetodo="";
public String tipo="";
public String estadoAnterior="";
public String estadoNuevo="";
public String mensaje="";
public long fecha=0;
public int leida=0;
public String getEmoji(){
if(TIPO_RECUPERADO.equals(tipo))return "🟢";
if(TIPO_RIESGO.equals(tipo))return "🟡";
if(TIPO_ALERTA.equals(tipo))return "🔴";
return "🔔";
}
public String getResumen(){
StringBuilder s=new StringBuilder();
s.append(getEmoji()).append(" ").append(nombreMetodo).append("\n");
s.append(mensaje).append("\n");
s.append(formatearFecha());
return s.toString();
}
public String formatearFecha(){
try{
java.text.SimpleDateFormat sdf=new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm",java.util.Locale.getDefault());
return sdf.format(new java.util.Date(fecha));
}catch(Exception e){return "";}
}
}
