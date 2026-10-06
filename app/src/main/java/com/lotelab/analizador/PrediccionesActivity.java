package com.lotelab.analizador;
import android.app.*;import android.os.*;import android.content.*;import android.database.*;import android.database.sqlite.*;import android.view.*;import android.widget.*;import android.graphics.*;import java.util.*;
public class PrediccionesActivity extends Activity{
LinearLayout listaPredicciones;
TextView resumenPred;
Button btnValidar,btnBorrarTodas;
@Override protected void onCreate(Bundle b){super.onCreate(b);setContentView(R.layout.activity_predicciones);
listaPredicciones=findViewById(R.id.listaPredicciones);
resumenPred=findViewById(R.id.resumenPred);
btnValidar=findViewById(R.id.btnValidar);
btnBorrarTodas=findViewById(R.id.btnBorrarTodas);
btnValidar.setOnClickListener(v->{new Thread(()->{validarTodas();runOnUiThread(()->cargarPredicciones());}).start();});
btnBorrarTodas.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Confirmar").setMessage("Borrar todas las predicciones?").setPositiveButton("Si",(d,w)->{new Thread(()->{DBHelper db=new DBHelper(this);db.getWritableDatabase().execSQL("DELETE FROM predicciones");db.close();runOnUiThread(()->cargarPredicciones());}).start();}).setNegativeButton("No",null).show());
cargarPredicciones();
}
void cargarPredicciones(){
listaPredicciones.removeAllViews();
DBHelper db=new DBHelper(this);
Cursor c=db.getReadableDatabase().rawQuery("SELECT id,fecha,rango,top5Fijo,top5C1,top5C2,top5Dec,top5Term,resultadoFijo,resultadoC1,resultadoC2,resultadoDec,resultadoTerm,aciertosFijo,aciertosC1,aciertosC2,aciertosDec,aciertosTerm,validada FROM predicciones ORDER BY id DESC",null);
int total=0,aciertos=0,fallos=0;
while(c.moveToNext()){
total++;
int validada=c.getInt(18);
LinearLayout fila=new LinearLayout(this);
fila.setOrientation(LinearLayout.VERTICAL);
fila.setPadding(10,10,10,10);
fila.setBackgroundColor(validada==1?Color.parseColor("#E8F5E9"):Color.parseColor("#FFFFFF"));
TextView tvFecha=new TextView(this);
String estado=validada==1?"Validada":"Pendiente";
tvFecha.setText("Fecha: "+c.getString(1)+"  |  Rango "+c.getString(2)+"  |  "+estado);
tvFecha.setTextSize(14);
tvFecha.setTextColor(Color.parseColor("#1A237E"));
tvFecha.setTypeface(null,Typeface.BOLD);
fila.addView(tvFecha);
TextView tvFijo=new TextView(this);
tvFijo.setText("Fijo top 5: "+c.getString(3)+(c.getString(8).isEmpty()?"":"  |  Resultado: "+c.getString(8)));
tvFijo.setTextSize(12);
tvFijo.setTextColor(Color.parseColor("#0D47A1"));
fila.addView(tvFijo);
TextView tvC1=new TextView(this);
tvC1.setText("C1 top 5: "+c.getString(4)+(c.getString(9).isEmpty()?"":"  |  Resultado: "+c.getString(9)));
tvC1.setTextSize(12);
tvC1.setTextColor(Color.parseColor("#1B5E20"));
fila.addView(tvC1);
TextView tvC2=new TextView(this);
tvC2.setText("C2 top 5: "+c.getString(5)+(c.getString(10).isEmpty()?"":"  |  Resultado: "+c.getString(10)));
tvC2.setTextSize(12);
tvC2.setTextColor(Color.parseColor("#E65100"));
fila.addView(tvC2);
TextView tvDec=new TextView(this);
tvDec.setText("Dec top 5: "+c.getString(6)+(c.getString(11).isEmpty()?"":"  |  Resultado: "+c.getString(11)));
tvDec.setTextSize(12);
tvDec.setTextColor(Color.parseColor("#4A148C"));
fila.addView(tvDec);
TextView tvTerm=new TextView(this);
tvTerm.setText("Term top 5: "+c.getString(7)+(c.getString(12).isEmpty()?"":"  |  Resultado: "+c.getString(12)));
tvTerm.setTextSize(12);
tvTerm.setTextColor(Color.parseColor("#F57F17"));
fila.addView(tvTerm);
if(validada==1){
int ac=c.getInt(13)+c.getInt(14)+c.getInt(15)+c.getInt(16)+c.getInt(17);
aciertos+=ac;
fallos+=(5-ac);
TextView tvAciertos=new TextView(this);
tvAciertos.setText("Aciertos: "+ac+" de 5");
tvAciertos.setTextSize(13);
tvAciertos.setTextColor(Color.parseColor("#1B5E20"));
tvAciertos.setTypeface(null,Typeface.BOLD);
fila.addView(tvAciertos);
}
listaPredicciones.addView(fila);
}
c.close();db.close();
if(total==0){resumenPred.setText("No hay predicciones guardadas");return;}
resumenPred.setText("Total: "+total+"  |  Aciertos: "+aciertos+"  |  Fallos: "+fallos+"  |  Tasa: "+(aciertos+fallos>0?(100*aciertos/(aciertos+fallos))+"%":"-"));
}
void validarTodas(){
DBHelper db=new DBHelper(this);
Cursor preds=db.getReadableDatabase().rawQuery("SELECT id,fecha,top5Fijo,top5C1,top5C2,top5Dec,top5Term FROM predicciones WHERE validada=0",null);
List<Integer> ids=new ArrayList<>();
List<String> fechas=new ArrayList<>();
List<String> topsF=new ArrayList<>();
List<String> topsC1=new ArrayList<>();
List<String> topsC2=new ArrayList<>();
List<String> topsD=new ArrayList<>();
List<String> topsT=new ArrayList<>();
while(preds.moveToNext()){
ids.add(preds.getInt(0));
fechas.add(preds.getString(1));
topsF.add(preds.getString(2));
topsC1.add(preds.getString(3));
topsC2.add(preds.getString(4));
topsD.add(preds.getString(5));
topsT.add(preds.getString(6));
}
preds.close();
for(int i=0;i<ids.size();i++){
String[] fF=topsF.get(i).split("\\s+");
String[] fC1=topsC1.get(i).split("\\s+");
String[] fC2=topsC2.get(i).split("\\s+");
String[] fD=topsD.get(i).split("\\s+");
String[] fT=topsT.get(i).split("\\s+");
Cursor sorteos=db.getReadableDatabase().rawQuery("SELECT date,tn,fijo,c1,c2,cent FROM draws WHERE date LIKE '%"+fechas.get(i)+"%'",null);
while(sorteos.moveToNext()){
String rFijo=sorteos.getString(2);
String rC1=sorteos.getString(3);
String rC2=sorteos.getString(4);
String rCent=sorteos.getString(5);
int aF=0,aC1=0,aC2=0,aD=0,aT=0;
if(enTop(rFijo,fF))aF=1;
if(enTop(rC1,fC1))aC1=1;
if(enTop(rC2,fC2))aC2=1;
int cent=parseInt(rCent);
if(cent>=0&&enTopDig(cent,fD))aD=1;
int term=parseInt(rFijo);
if(term>=0&&enTopDig(term%10,fT))aT=1;
ContentValues v=new ContentValues();
v.put("resultadoFijo",rFijo);
v.put("resultadoC1",rC1);
v.put("resultadoC2",rC2);
v.put("resultadoDec",rCent);
v.put("resultadoTerm",String.valueOf(term%10));
v.put("aciertosFijo",aF);
v.put("aciertosC1",aC1);
v.put("aciertosC2",aC2);
v.put("aciertosDec",aD);
v.put("aciertosTerm",aT);
v.put("validada",1);
db.getWritableDatabase().update("predicciones",v,"id=?",new String[]{String.valueOf(ids.get(i))});
break;
}
sorteos.close();
}
db.close();
}
boolean enTop(String num,String[] top5){
for(String t:top5){String n=t.split("\\(")[0];if(n.equals(num))return true;}
return false;
}
boolean enTopDig(int dig,String[] top5){
for(String t:top5){String n=t.split("\\(")[0];try{if(Integer.parseInt(n)==dig)return true;}catch(Exception e){}}
return false;
}
int parseInt(String s){try{return Integer.parseInt(s);}catch(Exception e){return -1;}}
static class DBHelper extends SQLiteOpenHelper{DBHelper(Context c){super(c,"loteria.db",null,2);}public void onCreate(SQLiteDatabase d){}public void onUpgrade(SQLiteDatabase d,int o,int n){if(o<2){d.execSQL("CREATE TABLE IF NOT EXISTS predicciones(id INTEGER PRIMARY KEY AUTOINCREMENT,fecha TEXT,rango TEXT,top5Fijo TEXT,top5C1 TEXT,top5C2 TEXT,top5Dec TEXT,top5Term TEXT,resultadoFijo TEXT DEFAULT '',resultadoC1 TEXT DEFAULT '',resultadoC2 TEXT DEFAULT '',resultadoDec TEXT DEFAULT '',resultadoTerm TEXT DEFAULT '',aciertosFijo INTEGER DEFAULT 0,aciertosC1 INTEGER DEFAULT 0,aciertosC2 INTEGER DEFAULT 0,aciertosDec INTEGER DEFAULT 0,aciertosTerm INTEGER DEFAULT 0,validada INTEGER DEFAULT 0)");}}}
}
