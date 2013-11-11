package tops.view;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.jfree.ui.RefineryUtilities;
import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.layout.Layout;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.activity.Activity;
import prefuse.controls.ControlAdapter;
import prefuse.controls.PanControl;
import prefuse.controls.ZoomControl;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.PolygonRenderer;
import prefuse.render.Renderer;
import prefuse.render.ShapeRenderer;
import prefuse.util.ColorLib;
import prefuse.util.GraphicsLib;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;
import tops.model.GraficoPizza;
import tops.parser.ConfigurationReader;
import tops.parser.ProbabilisticModelParameterValue;
import tops.parser.ProbabilisticModelParameters;




/**
 *
 * @author fabinhosano
 */
public class Aggregated extends Display {

    public static final String GRAPH = "graph";
    public static final String NODES = "graph.nodes";
    public static final String EDGES = "graph.edges";
    public static final String AGGR = "aggregates";

    public Aggregated() {
        
        super(new Visualization());
        initDataGroups();

        Renderer nodeR = new ShapeRenderer(20);
        
        Renderer polyR = new PolygonRenderer(Constants.POLY_TYPE_CURVE);
        ((PolygonRenderer) polyR).setCurveSlack(0.15f);

        DefaultRendererFactory drf = new DefaultRendererFactory();
        drf.setDefaultRenderer(nodeR);
        drf.add("ingroup('aggregates')", polyR);
        m_vis.setRendererFactory(drf);

        ColorAction nStroke = new ColorAction(NODES, VisualItem.STROKECOLOR);
        nStroke.setDefaultColor(ColorLib.gray(100));
        nStroke.add("_hover", ColorLib.gray(50));

        ColorAction nFill = new ColorAction(NODES, VisualItem.FILLCOLOR);
        nFill.setDefaultColor(ColorLib.gray(255));
        nFill.add("_hover", ColorLib.gray(200));

        ColorAction nEdges = new ColorAction(EDGES, VisualItem.STROKECOLOR);
        nEdges.setDefaultColor(ColorLib.gray(100));

        ColorAction aStroke = new ColorAction(AGGR, VisualItem.STROKECOLOR);
        aStroke.setDefaultColor(ColorLib.gray(200));
        aStroke.add("_hover", ColorLib.rgb(255, 100, 100));

        int[] palette = new int[]{
            ColorLib.rgba(255, 200, 200, 150),
            ColorLib.rgba(200, 255, 200, 150),
            ColorLib.rgba(200, 200, 255, 150),
            ColorLib.rgba(200, 155, 155, 100)
        };
        ColorAction aFill = new DataColorAction(AGGR, "id",
                Constants.NOMINAL, VisualItem.FILLCOLOR, palette);

        ActionList colors = new ActionList();
        colors.add(nStroke);
        colors.add(nFill);
        colors.add(nEdges);
        colors.add(aStroke);
        colors.add(aFill);

        ActionList layout = new ActionList(Activity.INFINITY);
        layout.add(colors);
        layout.add(new ForceDirectedLayout(GRAPH, true));
        layout.add(new AggregateLayout(AGGR));
        layout.add(new RepaintAction());
        m_vis.putAction("layout", layout);

        setSize(500, 500);
        pan(250, 250);
        setHighQuality(true);
        addControlListener(new AggregateDragControl());
        addControlListener(new ZoomControl());
        addControlListener(new PanControl());

        m_vis.run("layout");
    }

    private void initDataGroups() {
        int [] positions;
        
        Graph g = new Graph();
        Node[] nodes = new Node[7];
        
        for (int i = 0; i < 7; ++i) { // numero de repetições para criar os nós e ligar os mesmos
            nodes[i] = g.addNode();
            if (i % 2 != 0) {
                g.addEdge(nodes[i-1], nodes[i]);
            }else{
                if ((i > 0) && (i % 2 == 0)){
                    g.addEdge(nodes[i-2], nodes[i]);
                }
            }    
        }

        VisualGraph vg = m_vis.addGraph(GRAPH, g);
        m_vis.setInteractive(EDGES, null, false);
        m_vis.setValue(NODES, null, VisualItem.SHAPE,
                new Integer(Constants.SHAPE_ELLIPSE));

        AggregateTable at = m_vis.addAggregates(AGGR);
        at.addColumn(VisualItem.POLYGON, float[].class);
        at.addColumn("id", int.class);

        
        Iterator node = vg.nodes();
        for (int i = 0; i < 7; ++i) { // percorrer os nós para criar os Renderers
            AggregateItem aitem = (AggregateItem) at.addItem();
            aitem.setInt("id", i);
            for (int j = 0; j < 1; ++j) { // criando o Renderer para o(s) nó(s).
                aitem.addItem((VisualItem) node.next());
            }
        }
    }
    
    
    public static ProbabilisticModelParameterValue ProbModelParamVal(ProbabilisticModelParameters params, String name){
        ProbabilisticModelParameterValue v = params.getMandatoryParameterValue(name);
        String modelName = v.getString();
        System.out.println(modelName);
        return v;
    }  

    
    
    public static void main(String[] argv) {
        
        ConfigurationReader reader = new ConfigurationReader();
        JFileChooser filechooser = new JFileChooser();
        filechooser.showOpenDialog(null);
       
        ProbabilisticModelParameters params = reader.load(filechooser.getSelectedFile().getAbsolutePath());
        
        ProbabilisticModelParameterValue v = ProbModelParamVal(params, "model_name");
        
        v = ProbModelParamVal(params, "alphabet");
        ArrayList<String> alpha = v.getStringVector();
        for (String e : alpha) {
            System.out.println("e: " + e);
        }
        
        v = params.getMandatoryParameterValue("probabilities");
        HashMap<String, Double> prob_map = v.getDoubleMap();
        for (String e : prob_map.keySet()) {
            String partA = "", partB = "";
            String[] parts = e.split("\\|");
            partA = parts[0];
            partB = parts[1];
            System.out.println("a: " + partA + " b: " + partB + " prob: " + prob_map.get(e));
        }
        
        v = params.getOptionalParameterValue("lixao");
        System.out.println ("lixao: " + v.getDouble());
         
        
        JFrame frame = demo();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public static JFrame demo() {
        Aggregated ad = new Aggregated();
        JFrame frame = new JFrame("A g g r e g a t e d");
        frame.getContentPane().add(ad);
        frame.pack();
        return frame;
    }
}
class AggregateLayout extends Layout {

    private int m_margin = 5; 
    private double[] m_pts;  

    public AggregateLayout(String aggrGroup) {
        super(aggrGroup);
    }

    public void run(double frac) {

        AggregateTable aggr = (AggregateTable) m_vis.getGroup(m_group);

        int num = aggr.getTupleCount();
        if (num == 0) {
            return;
        }


        int maxsz = 0;
        for (Iterator aggrs = aggr.tuples(); aggrs.hasNext();) {
            maxsz = Math.max(maxsz, 4 * 2
                    * ((AggregateItem) aggrs.next()).getAggregateSize());
        }
        if (m_pts == null || maxsz > m_pts.length) {
            m_pts = new double[maxsz];
        }


        Iterator aggrs = m_vis.visibleItems(m_group);
        while (aggrs.hasNext()) {
            AggregateItem aitem = (AggregateItem) aggrs.next();

            int idx = 0;
            if (aitem.getAggregateSize() == 0) {
                continue;
            }
            VisualItem item = null;
            Iterator iter = aitem.items();
            while (iter.hasNext()) {
                item = (VisualItem) iter.next();
                if (item.isVisible()) {
                    addPoint(m_pts, idx, item, m_margin);
                    idx += 2 * 4;
                }
            }

            if (idx == 0) {
                continue;
            }


            double[] nhull = GraphicsLib.convexHull(m_pts, idx);


            float[] fhull = (float[]) aitem.get(VisualItem.POLYGON);
            if (fhull == null || fhull.length < nhull.length) {
                fhull = new float[nhull.length];
            } else if (fhull.length > nhull.length) {
                fhull[nhull.length] = Float.NaN;
            }


            for (int j = 0; j < nhull.length; j++) {
                fhull[j] = (float) nhull[j];
            }
            aitem.set(VisualItem.POLYGON, fhull);
            aitem.setValidated(false); 
        }
    }

    private static void addPoint(double[] pts, int idx,
            VisualItem item, int growth) {
        Rectangle2D b = item.getBounds();
        double minX = (b.getMinX()) - growth, minY = (b.getMinY()) - growth;
        double maxX = (b.getMaxX()) + growth, maxY = (b.getMaxY()) + growth;
        pts[idx] = minX;
        pts[idx + 1] = minY;
        pts[idx + 2] = minX;
        pts[idx + 3] = maxY;
        pts[idx + 4] = maxX;
        pts[idx + 5] = minY;
        pts[idx + 6] = maxX;
        pts[idx + 7] = maxY;
    }
} 


class AggregateDragControl extends ControlAdapter {

    private VisualItem activeItem;
    protected Point2D down = new Point2D.Double();
    protected Point2D temp = new Point2D.Double();
    protected boolean dragged;

   
    public AggregateDragControl() {
    }

    
    public void itemEntered(VisualItem item, MouseEvent e) {
        Display d = (Display) e.getSource();
        d.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        activeItem = item;
        if (!(item instanceof AggregateItem)) {
            setFixed(item, true);
        }
    }

    
    public void itemExited(VisualItem item, MouseEvent e) {
        if (activeItem == item) {
            activeItem = null;
            setFixed(item, false);
        }
        Display d = (Display) e.getSource();
        d.setCursor(Cursor.getDefaultCursor());
    }

    
    public void itemPressed(VisualItem item, MouseEvent e) {
        if (!SwingUtilities.isLeftMouseButton(e)) {
            return;
        }
        dragged = false;
        Display d = (Display) e.getComponent();
        d.getAbsoluteCoordinate(e.getPoint(), down);
        if (item instanceof AggregateItem) {
            setFixed(item, true);
        }
        
        if (e.getClickCount() == 2 && !e.isConsumed()) {
            GraficoPizza gp = new GraficoPizza();  
            gp.pack();  
            RefineryUtilities.centerFrameOnScreen(gp);  
            gp.setVisible(true);  
        }
    }
    
    
    public void itemPressedDoubleClick(VisualItem item, MouseEvent e) {
        
        if (!SwingUtilities.isLeftMouseButton(e)) {
            return;
        }
        
    }
    
    
    public void itemReleased(VisualItem item, MouseEvent e) {
        if (!SwingUtilities.isLeftMouseButton(e)) {
            return;
        }
        if (dragged) {
            activeItem = null;
            setFixed(item, false);
            dragged = false;
        }
    }

    
    public void itemDragged(VisualItem item, MouseEvent e) {
        if (!SwingUtilities.isLeftMouseButton(e)) {
            return;
        }
        dragged = true;
        Display d = (Display) e.getComponent();
        d.getAbsoluteCoordinate(e.getPoint(), temp);
        double dx = temp.getX() - down.getX();
        double dy = temp.getY() - down.getY();

        move(item, dx, dy);

        down.setLocation(temp);
    }

    protected static void setFixed(VisualItem item, boolean fixed) {
        if (item instanceof AggregateItem) {
            Iterator items = ((AggregateItem) item).items();
            while (items.hasNext()) {
                setFixed((VisualItem) items.next(), fixed);
            }
        } else {
            item.setFixed(fixed);
        }
    }

    protected static void move(VisualItem item, double dx, double dy) {
        if (item instanceof AggregateItem) {
            Iterator items = ((AggregateItem) item).items();
            while (items.hasNext()) {
                move((VisualItem) items.next(), dx, dy);
            }
        } else {
            double x = item.getX();
            double y = item.getY();
            item.setStartX(x);
            item.setStartY(y);
            item.setX(x + dx);
            item.setY(y + dy);
            item.setEndX(x + dx);
            item.setEndY(y + dy);
        }
    }
}
