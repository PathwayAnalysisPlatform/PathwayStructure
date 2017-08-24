# 
# This script takes the interaction iGraph data frames and plots their degree distributions.
#
startTimeAll <- proc.time()



## Libraries

library(igraph)
library(ggplot2)



## Parameters

colors <- colorRampPalette(c("black", "red"))(2017 - 1933)
singlePlots <- F


## Main script


# Load number of vertices and edges per year

print(paste(Sys.time(), " Loading timeline data", sep = ""))

years <- c()
nVertices <- c()
nEdges <- c()
diameter <- c()
clusters <- c()

degree <- c()
degreeDistribution <- c()
yearVector <- c()

for (year in 1934 : 2017) {
  
  print(paste(Sys.time(), " year ", year, sep = ""))
  
  years <- c(years, year)
  
  baseName <- paste("resources/iGraph/reactome/timeline/reactome_", year, sep = "")
  
  vertices <- read.table(paste(baseName, "_vertices", sep = ""), header = T, sep = "\t", stringsAsFactors = F, quote = "", comment.char = "")
  edges <- read.table(paste(baseName, "_edges", sep = ""), header = T, sep = " ", stringsAsFactors = F, quote = "", comment.char = "")
  
  nVertices <- c(nVertices, nrow(vertices))
  nEdges <- c(nEdges, nrow(edges))
  
  graph <- graph_from_data_frame(d = edges, vertices = vertices, directed = F)
  
  clusters <- c(clusters, length(groups(components(graph))))
  
  diameter <- c(diameter, diameter(graph))
  
  yearDistribution <- degree.distribution(graph) * nrow(vertices)
  yearDegree <- 0:(length(yearDistribution)-1)
  
  degree <- c(degree, yearDegree)
  degreeDistribution <- c(degreeDistribution, yearDistribution)
  yearVector <- c(yearVector, rep(year, length(yearDegree)))
  
  
  if (singlePlots) {
    
    dPlotData <- data.frame(yearDegree, yearDistribution, stringsAsFactors = F)
    dPlotData <- dPlotData[dPlotData$yearDegree > 0 & dPlotData$yearDistribution > 0, ]
    
    dPlot <- ggplot()
    dPlot <- dPlot + geom_point(data = dPlotData, aes(x = yearDegree, y = yearDistribution), col = colors[year-1933], alpha = 0.3, size = 3)
    dPlot <- dPlot + theme_bw(base_size = 22)
    dPlot <- dPlot + scale_color_manual(name = element_blank(), values = colors)
    dPlot <- dPlot + scale_x_log10(name = "degree", limits = c(1, 5000))
    dPlot <- dPlot + scale_y_log10(name = "# Proteins", limits = c(1, 1000))
    dPlot <- dPlot + theme(legend.position = 'none',
                           plot.title = element_text(hjust = 1))
    dPlot <- dPlot + ggtitle(as.character(year))
    
    
    png(paste("resources/iGraph/plots/timeline/degreeDistributions_", year, ".png", sep = ""), width = 600, height = 800)
    plot(dPlot)
    dummy <- dev.off()
    
  }
  
}

print(paste(Sys.time(), " Plotting size per year", sep = ""))

sizeDataFrame <- data.frame(years, nVertices, nEdges, clusters, diameter, stringsAsFactors = F)
sizeDataFrame$yearsAsFactor <- as.factor(sizeDataFrame$years)
sizeDataFrame$labels <- ""
sizeDataFrame$labels[1] <- 1934
sizeDataFrame$labels[21] <- 1934+20
sizeDataFrame$labels[32] <- 1934+31
sizeDataFrame$labels[34] <- 1934+33
sizeDataFrame$labels[37] <- 1970
sizeDataFrame$labels[47] <- 1980
sizeDataFrame$labels[57] <- 1990
sizeDataFrame$labels[67] <- 2000
sizeDataFrame$labels[77] <- 2010


# Plot vertices per year

vPlot <- ggplot()
vPlot <- vPlot + geom_point(data = sizeDataFrame, aes(x = years, y = nVertices, col = yearsAsFactor), alpha = 0.8, size = 8)
vPlot <- vPlot + theme_bw(base_size = 33)
vPlot <- vPlot + scale_color_manual(name = element_blank(), values = colors)
vPlot <- vPlot + scale_x_continuous(name = "Year")
vPlot <- vPlot + scale_y_continuous(name = "# Proteins")
vPlot <- vPlot + theme(legend.position = 'none')
vPlot <- vPlot + geom_text_repel(data = sizeDataFrame, aes(x = years, y = nVertices, label = labels), point.padding = unit(1, 'lines'), size = 8)


png("resources/iGraph/plots/timeline/vertices.png", width = 800, height = 600)
plot(vPlot)
dummy <- dev.off()


# Plot edges per year

ePlot <- ggplot()
ePlot <- ePlot + geom_point(data = sizeDataFrame, aes(x = years, y = nEdges, col = yearsAsFactor), alpha = 0.8, size = 8)
ePlot <- ePlot + theme_bw(base_size = 33)
ePlot <- ePlot + scale_color_manual(name = element_blank(), values = colors)
ePlot <- ePlot + scale_x_continuous(name = "Year")
ePlot <- ePlot + scale_y_continuous(name = "# Links")
ePlot <- ePlot + theme(legend.position = 'none')
ePlot <- ePlot + geom_text_repel(data = sizeDataFrame, aes(x = years, y = nEdges, label = labels), point.padding = unit(1, 'lines'), size = 8)


png("resources/iGraph/plots/timeline/edges.png", width = 800, height = 600)
plot(ePlot)
dummy <- dev.off()


# Plot vertices and edges per year

evPlot <- ggplot()
evPlot <- evPlot + geom_point(data = sizeDataFrame, aes(x = nVertices, y = nEdges, col = yearsAsFactor), alpha = 0.5, size = 8)
evPlot <- evPlot + geom_vline(aes(xintercept = 20201), col = "green", linetype = "dashed")
evPlot <- evPlot + theme_bw(base_size = 33)
evPlot <- evPlot + scale_color_manual(name = element_blank(), values = colors)
evPlot <- evPlot + scale_x_log10(name = "# Proteins")
evPlot <- evPlot + scale_y_log10(name = "# Links")
evPlot <- evPlot + theme(legend.position = 'none')
evPlot <- evPlot + geom_text_repel(data = sizeDataFrame, aes(x = nVertices, y = nEdges, label = labels), point.padding = unit(1, 'lines'), size = 8)


png("resources/iGraph/plots/timeline/edges-vertices.png", width = 800, height = 600)
plot(evPlot)
dummy <- dev.off()


# Plotting size information

cdPlot <- ggplot()
cdPlot <- cdPlot + geom_point(data = sizeDataFrame, aes(x = clusters, y = diameter, col = yearsAsFactor), alpha = 0.5, size = 8)
cdPlot <- cdPlot + geom_vline(aes(xintercept = 20201), col = "green", linetype = "dashed")
cdPlot <- cdPlot + theme_bw(base_size = 33)
cdPlot <- cdPlot + scale_color_manual(name = element_blank(), values = colors)
cdPlot <- cdPlot + scale_x_log10(name = "# Clusters")
cdPlot <- cdPlot + scale_y_log10(name = "Diameter")
cdPlot <- cdPlot + theme(legend.position = 'none')
cdPlot <- cdPlot + geom_text_repel(data = sizeDataFrame, aes(x = clusters, y = diameter, label = labels), point.padding = unit(1, 'lines'), size = 8)


png("resources/iGraph/plots/timeline/cluster-diameter.png", width = 800, height = 600)
plot(cdPlot)
dummy <- dev.off()