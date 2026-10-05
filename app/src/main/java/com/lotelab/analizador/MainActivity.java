package com.lotelab.analizador;
import android.app.*;import android.os.*;import android.content.*;import android.database.*;import android.database.sqlite.*;import android.net.Uri;import android.provider.OpenableColumns;import android.view.*;import android.widget.*;import android.graphics.*;import java.io.*;import java.nio.charset.StandardCharsets;import java.util.*;import java.util.zip.*;import javax.xml.parsers.*;import org.w3c.dom.*;
public class MainActivity extends Activity{
DB db;TextView estado,salida;TableLayout tabla;static final int PICK=10;
@Override public void onCreate(Bundle b){super.onCreate(b);setContentView(R.layout.activity_main);db=new DB(this);estado=findViewById(R.id.estado);salida=findViewById(R.id.salida);tabla=findViewById(R.id.tablaResultados);
findViewById(R.id.btnImportar).setOnClickListener(v->pick());
findViewById(R.id.btnNuevo).setOnClickListener(v->nuevo());
findViewById(R.id.btnCalcular).setOnClickListener(v->calcular());
findViewById(R.id.btnHistorial).setOnClickListener(v->verHistorial());
findViewById(R.id.btnResultados).setOnClickListener(v->calcular());
findViewById(R.id.btnExportar).setOnClickListener(v->exportar());
if(!getPreferences(0).getBoolean("init",false)){new Thread(()->{try{InputStream in=getAssets().open("Florida_inicial.tsv");int n=Importer.importStream(db,in,"Florida_inicial.tsv");getPreferences(0).edit().putBoolean("init",true).apply();runOnUiThread(()->estado.setText("Historial cargado: "+n));}catch(Exception e){runOnUiThread(()->estado.setText("Error: "+e.getMessage()));}}).start();}else estado.setText("Sorteos: "+db.count());}
void pick(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("*/*");startActivityForResult(i,PICK);}
@Override protected void onActivityResult(int r,int c,Intent d){super.onActivityResult(r,c,d);if(r==PICK&&c==RESULT_OK&&d!=null){Uri u=d.getData();new Thread(()->{try{InputStream in=getContentResolver().openInputStream(u);int n=Importer.importStream(db,in,getName(u));runOnUiThread(()->estado.setText("Importados: "+n+" Total: "+db.count()));}catch(Exception e){runOnUiThread(()->Toast.makeText(this,"Error: "+e.getMessage(),Toast.LENGTH_LONG).show());}}).start();}}
String getName(Uri u){Cursor c=getContentResolver().query(u,null,null,null,null);if(c!=null){try{int x=c.getColumnIndex(OpenableColumns.DISPLAY_NAME);if(c.moveToFirst()&&x>=0)return c.getString(x);}finally{c.close();}}return "archivo";}
void nuevo(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);String[] h={"Fecha dd-MM-yyyy","T/N","Centena","Fijo","Corrido 1","Corrido 2"};EditText[] e=new EditText[6];for(int i=0;i<6;i++){e[i]=new EditText(this);e[i].setHint(h[i]);l.addView(e[i]);}new AlertDialog.Builder(this).setTitle("Agregar sorteo").setView(l).setPositiveButton("Guardar",(x,w)->{try{db.insert(new Draw(e[0].getText().toString(),e[1].getText().toString(),e[2].getText().toString(),pad(e[3].getText().toString()),pad(e[4].getText().toString()),pad(e[5].getText().toString())));estado.setText("Total: "+db.count());}catch(Exception z){Toast.makeText(this,"Error: "+z.getMessage(),Toast.LENGTH_LONG).show();}}).setNegativeButton("Cancelar",null).show();}
String pad(String s){s=s.trim();if(s.length()==1)s="0"+s;return s;}
void calcular(){new Thread(()->{Stats s=Stats.calc(db);runOnUiThread(()->{dibujarTabla(s);estado.setText("Listo. "+db.count()+" sorteos.");});}).start();}
void dibujarTabla(Stats s){tabla.removeAllViews();tabla.setBackgroundColor(Color.parseColor("#9E9E9E"));
String[] headers={"Rango","Fijo","Corrido 1","Corrido 2","Decenas","Terminales"};
String[] colores={"#FFFFFF","#1565C0","#2E7D32","#EF6C00","#6A1B9A","#F9A825"};
TableRow encabezado=new TableRow(this);
for(int i=0;i<6;i++){encabezado.addView(celda(headers[i],colores[i],Color.WHITE,true));}
tabla.addView(encabezado);
for(int r=0;r<6;r++){TableRow fila=new TableRow(this);
fila.addView(celda(Stats.R[r],"#FFFFFF",Color.parseColor("#1A237E"),true));
fila.addView(celda(s.topsFijo[r],"#FFFFFF",Color.parseColor("#0D47A1"),false));
fila.addView(celda(s.topsC1[r],"#FFFFFF",Color.parseColor("#1B5E20"),false));
fila.addView(celda(s.topsC2[r],"#FFFFFF",Color.parseColor("#E65100"),false));
fila.addView(celda(s.topsDec[r],"#FFFFFF",Color.parseColor("#4A148C"),false));
fila.addView(celda(s.topsTerm[r],"#FFFFFF",Color.parseColor("#F57F17"),false));
tabla.addView(fila);}}
TextView celda(String txt,String bg,int colorTexto,boolean negrita){TextView tv=new TextView(this);tv.setText(txt);tv.setPadding(8,8,8,8);tv.setTextSize(11);tv.setTextColor(colorTexto);tv.setBackgroundColor(Color.parseColor(bg));if(negrita)tv.setTypeface(null,Typeface.BOLD);return tv;}
void verHistorial(){StringBuilder b=new StringBuilder();Cursor c=db.all();int n=0;while(c.moveToNext()&&n<100){b.append(c.getString(1)).append(" | ").append(c.getString(2)).append(" | ").append(c.getString(3)).append(" | ").append(c.getString(4)).append(" | ").append(c.getString(5)).append(" | ").append(c.getString(6)).append(" -- ");n++;}c.close();salida.setText("Primeros "+n+": "+b);}
void exportar(){try{File f=new File(getExternalFilesDir(null),"historial.csv");FileWriter w=new FileWriter(f);w.write("Fecha,TN,Centena,Fijo,C1,C2\n");Cursor c=db.all();while(c.moveToNext())w.write(c.getString(1)+","+c.getString(2)+","+c.getString(3)+","+c.getString(4)+","+c.getString(5)+","+c.getString(6)+"\n");c.close();w.close();new AlertDialog.Builder(this).setTitle("Listo").setMessage(f.getAbsolutePath()).setPositiveButton("OK",null).show();}catch(Exception e){Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();}}
static class Draw{String date,tn,cent,f,c1,c2;Draw(String a,String b,String c,String d,String e,String f){date=a;tn=b;cent=c;this.f=d;c1=e;c2=f;}}
static class DB extends SQLiteOpenHelper{DB(Context c){super(c,"loteria.db",null,1);}public void onCreate(SQLiteDatabase d){d.execSQL("CREATE TABLE draws(id INTEGER PRIMARY KEY AUTOINCREMENT,date TEXT,tn TEXT,cent TEXT,fijo TEXT,c1 TEXT,c2 TEXT,UNIQUE(date,tn,fijo,c1,c2))");}public void onUpgrade(SQLiteDatabase d,int o,int n){}void insert(Draw x){ContentValues v=new ContentValues();v.put("date",x.date);v.put("tn",x.tn);v.put("cent",x.cent);v.put("fijo",x.f);v.put("c1",x.c1);v.put("c2",x.c2);getWritableDatabase().insertWithOnConflict("draws",null,v,SQLiteDatabase.CONFLICT_IGNORE);}int count(){Cursor c=getReadableDatabase().rawQuery("select count(*) from draws",null);c.moveToFirst();int n=c.getInt(0);c.close();return n;}Cursor all(){return getReadableDatabase().query("draws",null,null,null,null,null,"id ASC");}}
static class Importer{
static int importStream(DB db,InputStream in,String name)throws Exception{byte[] data=readAll(in);String lower=name.toLowerCase();if(isZip(data)){if(lower.endsWith(".xlsx"))return xlsx(db,data);if(lower.endsWith(".docx"))return docx(db,data);}return text(db,new String(data,StandardCharsets.UTF_8));}
static boolean isZip(byte[] d){return d.length>4&&d[0]=='P'&&d[1]=='K';}
static int text(DB db,String s){int n=0;String[] lines=s.split("\\r?\\n");for(String line:lines){String[] p=line.contains("\t")?line.split("\\t",-1):line.split("[,;]",-1);if(p.length<6)continue;if(p[0].toLowerCase().contains("fecha"))continue;String date=p[0].trim(),tn=p[1].trim(),cent=p[2].trim(),f=clean(p[3]),c1=clean(p[4]),c2=clean(p[5]);if(valid(date,f,c1,c2)){db.insert(new Draw(date,tn,cent,f,c1,c2));n++;}}return n;}
static String clean(String x){x=x.trim();x=x.replace("\"","");if(x.length()==1)x="0"+x;return x;}
static boolean valid(String date,String f,String c1,String c2){return date.length()>0&&f.matches("\\d{2}")&&c1.matches("\\d{2}")&&c2.matches("\\d{2}");}
static int xlsx(DB db,byte[] data)throws Exception{ZipInputStream z=new ZipInputStream(new ByteArrayInputStream(data));Map<String,String> files=new HashMap<>();ZipEntry e;while((e=z.getNextEntry())!=null){if(!e.isDirectory()&&(e.getName().equals("xl/sharedStrings.xml")||e.getName().matches("xl/worksheets/sheet\\d+\\.xml")))files.put(e.getName(),new String(readAll(z),StandardCharsets.UTF_8));}String ss=files.get("xl/sharedStrings.xml");List<String> shared=new ArrayList<>();if(ss!=null){Document d=xml(ss);NodeList ts=d.getElementsByTagName("t");for(int i=0;i<ts.getLength();i++)shared.add(ts.item(i).getTextContent());}String sheet=files.get("xl/worksheets/sheet1.xml");if(sheet==null)return 0;Document d=xml(sheet);NodeList rows=d.getElementsByTagName("row");int n=0;for(int i=0;i<rows.getLength();i++){NodeList cells=((Element)rows.item(i)).getElementsByTagName("c");Map<Integer,String> vals=new HashMap<>();for(int j=0;j<cells.getLength();j++){Element c=(Element)cells.item(j);String ref=c.getAttribute("r");int col=0;for(char ch:ref.toCharArray()){if(Character.isLetter(ch))col=col*26+(Character.toUpperCase(ch)-'A'+1);else break;}col--;NodeList v=c.getElementsByTagName("v");if(v.getLength()>0){String val=v.item(0).getTextContent();if("s".equals(c.getAttribute("t"))){int ix=Integer.parseInt(val);if(ix<shared.size())val=shared.get(ix);}vals.put(col,val);}}if(vals.size()>=6&&!vals.getOrDefault(0,"").toLowerCase().contains("fecha")){String date=vals.getOrDefault(0,"");String tn=vals.getOrDefault(1,"");String cent=vals.getOrDefault(2,"");String f=clean(vals.getOrDefault(3,"")),c1=clean(vals.getOrDefault(4,"")),c2=clean(vals.getOrDefault(5,""));if(valid(date,f,c1,c2)){db.insert(new Draw(date,tn,cent,f,c1,c2));n++;}}}return n;}
static int docx(DB db,byte[] data)throws Exception{ZipInputStream z=new ZipInputStream(new ByteArrayInputStream(data));String xml=null;ZipEntry e;while((e=z.getNextEntry())!=null){if(e.getName().equals("word/document.xml")){xml=new String(readAll(z),StandardCharsets.UTF_8);break;}}if(xml==null)return 0;Document d=xml(xml);StringBuilder b=new StringBuilder();NodeList ts=d.getElementsByTagName("t");for(int i=0;i<ts.getLength();i++)b.append(ts.item(i).getTextContent()).append(i%6==5?'\n':'\t');return text(db,b.toString());}
static Document xml(String s)throws Exception{DocumentBuilderFactory f=DocumentBuilderFactory.newInstance();f.setNamespaceAware(true);return f.newDocumentBuilder().parse(new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8)));}
static byte[] readAll(InputStream in)throws IOException{ByteArrayOutputStream b=new ByteArrayOutputStream();byte[] x=new byte[8192];int n;while((n=in.read(x))>0)b.write(x,0,n);return b.toByteArray();}}
static class Stats{
static String[] R={"1-5","6-10","11-15","16-20","21-25","26-30"};
String[] topsFijo=new String[6];String[] topsC1=new String[6];String[] topsC2=new String[6];String[] topsDec=new String[6];String[] topsTerm=new String[6];
static int range(String date){try{String[] q=date.split("[-/]");int day=Integer.parseInt(q[0]);return Math.min(5,(day-1)/5);}catch(Exception e){return -1;}}
static int num(String s){try{return Integer.parseInt(s);}catch(Exception e){return -1;}}
static String[] top3(int[] cnt){int[] ix=new int[100];for(int i=0;i<100;i++)ix[i]=i;for(int i=0;i<100;i++)for(int j=i+1;j<100;j++)if(cnt[ix[j]]>cnt[ix[i]]){int t=ix[i];ix[i]=ix[j];ix[j]=t;}String[] res=new String[3];for(int k=0;k<3;k++){res[k]=String.format("%02d",ix[k])+"("+cnt[ix[k]]+")";}return res;}
static String[] top3dig(int[] cnt){int[] ix=new int[10];for(int i=0;i<10;i++)ix[i]=i;for(int i=0;i<10;i++)for(int j=i+1;j<10;j++)if(cnt[ix[j]]>cnt[ix[i]]){int t=ix[i];ix[i]=ix[j];ix[j]=t;}String[] res=new String[3];for(int k=0;k<3;k++){res[k]=ix[k]+"("+cnt[ix[k]]+")";}return res;}
static Stats calc(DB db){
int[][][] n=new int[6][3][100];
int[][][] dec=new int[6][3][10];
int[][][] term=new int[6][3][10];
int total=0;
Cursor c=db.all();
while(c.moveToNext()){
int r=range(c.getString(1));if(r<0)continue;
String[] a={c.getString(4),c.getString(5),c.getString(6)};
for(int k=0;k<3;k++){
int x=num(a[k]);
if(x>=0&&x<100){
n[r][k][x]++;
dec[r][k][x/10]++;
term[r][k][x%10]++;
}
}
total++;
}
c.close();
Stats s=new Stats();
for(int r=0;r<6;r++){
String[] f=top3(n[r][0]);s.topsFijo[r]=f[0]+" "+f[1]+" "+f[2];
String[] c1=top3(n[r][1]);s.topsC1[r]=c1[0]+" "+c1[1]+" "+c1[2];
String[] c2=top3(n[r][2]);s.topsC2[r]=c2[0]+" "+c2[1]+" "+c2[2];
String[] d=top3dig(dec[r][0]);s.topsDec[r]=d[0]+" "+d[1]+" "+d[2];
String[] t=top3dig(term[r][0]);s.topsTerm[r]=t[0]+" "+t[1]+" "+t[2];
}
return s;
}
}
}
