package com.lotelab.analizador;
import android.app.*;import android.os.*;import android.graphics.*;import android.view.*;import android.widget.*;import java.util.*;
public class MetodosActivity extends Activity{
LinearLayout contenedor,contenedorAlertas;
TextView lblContador,lblResumen,lblAlertasTitulo;
List<Metodo> metodos=new ArrayList<>();
List<Alerta> alertas=new ArrayList<>();
@Override protected void onCreate(Bundle b){super.onCreate(b);setContentView(R.layout.activity_metodos);
contenedor=findViewById(R.id.contenedor);
contenedorAlertas=findViewById(R.id.contenedorAlertas);
lblContador=findViewById(R.id.lblContador);
lblResumen=findViewById(R.id.lblResumen);
lblAlertasTitulo=findViewById(R.id.lblAlertasTitulo);
findViewById(R.id.btnVolver).setOnClickListener(v->finish());
cargarTodo();
}
@Override protected void onResume(){super.onResume();cargarTodo();}
void cargarTodo(){
metodos=MetodoManager.listar(this);
alertas=AlertaManager.listarNoLeidas(this);
lblContador.setText(metodos.size()+" metodos");
int cumpliendo=0,riesgo=0,alerta=0;
for(Metodo m:metodos){
if("CUMPLIENDO".equals(m.estado))cumpliendo++;
else if("RIESGO".equals(m.estado))riesgo++;
else if("ALERTA".equals(m.estado))alerta++;
}
lblResumen.setText("🟢 "+cumpliendo+"  🟡 "+riesgo+"  🔴 "+alerta);
dibujarAlertas();
dibujarLista();
}
void dibujarAlertas(){
contenedorAlertas.removeAllViews();
if(alertas.isEmpty()){
lblAlertasTitulo.setVisibility(View.GONE);
return;
}
lblAlertasTitulo.setVisibility(View.VISIBLE);
lblAlertasTitulo.setText("🔔 ALERTAS ("+alertas.size()+" sin leer)");
for(final Alerta a:alertas){
LinearLayout card=new LinearLayout(this);
card.setOrientation(LinearLayout.VERTICAL);
card.setPadding(14,10,14,10);
card.setBackgroundColor(Color.parseColor("#FFF9C4"));
LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
lp.setMargins(0,0,0,6);
card.setLayoutParams(lp);
TextView tvTitulo=new TextView(this);
tvTitulo.setText(a.getEmoji()+" "+a.nombreMetodo);
tvTitulo.setTextSize(13);
tvTitulo.setTypeface(null,Typeface.BOLD);
tvTitulo.setTextColor(Color.parseColor("#0D47A1"));
card.addView(tvTitulo);
TextView tvMsg=new TextView(this);
tvMsg.setText(a.mensaje);
tvMsg.setTextSize(12);
tvMsg.setTextColor(Color.parseColor("#424242"));
card.addView(tvMsg);
TextView tvFecha=new TextView(this);
tvFecha.setText(a.formatearFecha());
tvFecha.setTextSize(10);
tvFecha.setTextColor(Color.parseColor("#757575"));
card.addView(tvFecha);
card.setOnClickListener(v->{
new AlertDialog.Builder(this).setTitle("Marcar como leida?").setMessage(a.nombreMetodo)
.setPositiveButton("Si",(d,w)->{AlertaManager.marcarComoLeida(this,a.id);cargarTodo();})
.setNegativeButton("No",null).show();
});
contenedorAlertas.addView(card);
}
LinearLayout.LayoutParams lpBtn=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
lpBtn.setMargins(0,6,0,16);
Button btnMarcar=new Button(this);
btnMarcar.setText("Marcar todas como leidas");
btnMarcar.setTextSize(12);
btnMarcar.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#F57F17")));
btnMarcar.setTextColor(Color.WHITE);
btnMarcar.setLayoutParams(lpBtn);
final int totalAlertas=alertas.size();
btnMarcar.setOnClickListener(v->{
new AlertDialog.Builder(this).setTitle("Marcar todas?").setMessage("Marcar "+totalAlertas+" alertas como leidas?")
.setPositiveButton("Si",(d,w)->{AlertaManager.marcarTodasComoLeidas(this);NotificacionHelper.cancelar(this);cargarTodo();})
.setNegativeButton("No",null).show();
});
contenedorAlertas.addView(btnMarcar);
}
void dibujarLista(){
contenedor.removeAllViews();
if(metodos.isEmpty()){
TextView tv=new TextView(this);
tv.setText("No hay metodos guardados.\n\nVe a Test Cumplimiento y guarda uno.");
tv.setTextSize(14);
tv.setTextColor(Color.parseColor("#616161"));
tv.setPadding(20,40,20,20);
tv.setGravity(Gravity.CENTER);
contenedor.addView(tv);
return;
}
for(final Metodo m:metodos){
LinearLayout card=new LinearLayout(this);
card.setOrientation(LinearLayout.VERTICAL);
card.setPadding(14,14,14,14);
card.setBackgroundColor(Color.WHITE);
LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
lp.setMargins(0,0,0,10);
card.setLayoutParams(lp);
int colorBorde;
if("CUMPLIENDO".equals(m.estado))colorBorde=Color.parseColor("#1B5E20");
else if("RIESGO".equals(m.estado))colorBorde=Color.parseColor("#F57F17");
else if("ALERTA".equals(m.estado))colorBorde=Color.parseColor("#C62828");
else colorBorde=Color.parseColor("#757575");
View borde=new View(this);
borde.setBackgroundColor(colorBorde);
borde.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,6));
card.addView(borde);
boolean tieneAlerta=false;
for(Alerta a:alertas){if(a.metodoId==m.id){tieneAlerta=true;break;}}
TextView tvNombre=new TextView(this);
tvNombre.setText(m.getEstadoEmoji()+" "+(tieneAlerta?"🔔 ":"")+m.nombre);
tvNombre.setTextSize(14);
tvNombre.setTypeface(null,Typeface.BOLD);
tvNombre.setTextColor(Color.parseColor("#0D47A1"));
tvNombre.setPadding(0,8,0,6);
card.addView(tvNombre);
TextView tvInfo=new TextView(this);
StringBuilder sb=new StringBuilder();
sb.append("Categoria: ").append(m.categoria);
sb.append(" | Turno: ").append(m.turno);
sb.append("\nDias: ").append(m.getDiasComoTexto());
sb.append("\nPool: ").append(m.tamanoPool).append(" numeros");
sb.append("\nCumplimiento: ").append(m.totalCumplidos).append("/").append(m.totalMeses);
if(m.totalMeses>0)sb.append(" (").append(String.format("%.1f",100.0*m.totalCumplidos/m.totalMeses)).append("%)");
sb.append("\nRacha actual: ").append(m.rachaActual).append(" meses");
sb.append(" | Mayor: ").append(m.mayorRacha);
if(!m.ultimoFallo.isEmpty())sb.append("\nUltimo fallo: ").append(m.ultimoFallo);
tvInfo.setText(sb.toString());
tvInfo.setTextSize(12);
tvInfo.setTextColor(Color.parseColor("#424242"));
tvInfo.setLineSpacing(0,1.3f);
card.addView(tvInfo);
card.setOnClickListener(v->mostrarOpciones(m));
contenedor.addView(card);
}
}
void mostrarOpciones(final Metodo m){
new AlertDialog.Builder(this).setTitle(m.getEstadoEmoji()+" "+m.nombre)
.setItems(new String[]{"📋 Ver pool completo","🗑️ Eliminar metodo"},(d,w)->{
if(w==0)verPool(m);
else if(w==1)confirmarEliminar(m);
}).show();
}
void verPool(Metodo m){
List<Integer> nums=m.getNumeros();
StringBuilder sb=new StringBuilder();
sb.append("Categoria: ").append(m.categoria).append("\n");
sb.append("Turno: ").append(m.turno).append("\n");
sb.append("Dias: ").append(m.getDiasComoTexto()).append("\n");
sb.append("Tamano: ").append(m.tamanoPool).append("\n\n");
sb.append("Pool:\n");
boolean esDigito="CENTENA".equals(m.categoria)||"DECENA".equals(m.categoria)||"TERMINAL".equals(m.categoria);
for(int i=0;i<nums.size();i++){
if(esDigito)sb.append(nums.get(i));
else sb.append(String.format("%02d",nums.get(i)));
if(i<nums.size()-1)sb.append(", ");
if((i+1)%10==0)sb.append("\n");
}
ScrollView sv=new ScrollView(this);
TextView tv=new TextView(this);
tv.setText(sb.toString());
tv.setTextSize(13);
tv.setPadding(30,30,30,30);
sv.addView(tv);
new AlertDialog.Builder(this).setTitle("Pool").setView(sv).setPositiveButton("OK",null).show();
}
void confirmarEliminar(final Metodo m){
new AlertDialog.Builder(this).setTitle("Eliminar metodo")
.setMessage("Seguro que quieres eliminar:\n\n"+m.nombre+"?")
.setPositiveButton("Eliminar",(d,w)->{
MetodoManager.eliminar(this,m.id);
Toast.makeText(this,"Metodo eliminado",Toast.LENGTH_SHORT).show();
cargarTodo();
})
.setNegativeButton("Cancelar",null).show();
}
}
