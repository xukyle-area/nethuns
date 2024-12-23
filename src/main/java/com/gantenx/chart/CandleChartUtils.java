package com.gantenx.chart;

import com.gantenx.constant.Series;
import com.gantenx.engine.Order;
import com.gantenx.model.Kline;
import com.gantenx.model.Pair;
import com.gantenx.utils.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.gantenx.constant.Constants.*;
import static com.gantenx.utils.DateUtils.SIMPLE_DATE_FORMAT;
import static org.jfree.chart.axis.AxisLocation.BOTTOM_OR_LEFT;

@Slf4j
public class CandleChartUtils {




}
