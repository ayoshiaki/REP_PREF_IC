/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tops.model;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 *
 * @author fabinhosano
 */
public class GraficoPizza extends ApplicationFrame{
    private static final long serialVersionUID = 1L;  
  
    public GraficoPizza() {  
        super(null);  
        this.setTitle("Grafico de Pizza");  
        JPanel jpanel = PanelDemostracao();  
        jpanel.setPreferredSize(new Dimension(500, 270));  
        setContentPane(jpanel);
    }  
  
    private static PieDataset criaDadosGrafico() {  
        DefaultPieDataset defaultpiedataset = new DefaultPieDataset();  
        defaultpiedataset.setValue("A", 25D);  
        defaultpiedataset.setValue("C", 25D);  
        defaultpiedataset.setValue("G", 25D);  
        defaultpiedataset.setValue("T", 25D);  
        return defaultpiedataset;  
    }  
  
    private static JFreeChart criaGrafico(PieDataset piedataset) {  
        JFreeChart jfreechart = ChartFactory.createPieChart(  
                "Gráfico Pizza Demo ", piedataset, true, true, false);  
        PiePlot plotagem = (PiePlot) jfreechart.getPlot();  
        plotagem.setLabelGenerator(new StandardPieSectionLabelGenerator(  
                "{0} ({2})"));//define porcentagem no gráfico  
        plotagem.setLabelBackgroundPaint(new Color(220, 220, 220));  
        return jfreechart;  
    }  
  
    public static JPanel PanelDemostracao() {  
        JFreeChart jfreechart = criaGrafico(criaDadosGrafico());  
        return new ChartPanel(jfreechart);  
    }  
  
}
