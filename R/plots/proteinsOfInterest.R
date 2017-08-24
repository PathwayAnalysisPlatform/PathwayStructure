# 
# This script exports proteins of interest from the network of Reactome.
#
startTimeAll <- proc.time()



## Libraries

library(igraph)
library(ggplot2)


## Main script


# Load data

print(paste(Sys.time(), " Loading data", sep = ""))

verticesReactome <- read.table("resources/iGraph/reactome/reactome_18.08.17_vertices", header = T, sep = "\t", stringsAsFactors = F, quote = "", comment.char = "")
edgesReactome <- read.table("resources/iGraph/reactome/reactome_18.08.17_edges", header = T, sep = " ", stringsAsFactors = F, quote = "", comment.char = "")

reactome <- graph_from_data_frame(d=edgesReactome, vertices=verticesReactome, directed=T)

reactomeDegrees <- degree(reactome, mode = "all")
reactomeDistribution <- degree.distribution(reactome, mode = "all")
reactomeDistributionScaled <- reactomeDistribution * nrow(verticesReactome)

# Plot degree distribution

degree <- 0:(length(reactomeDistributionScaled)-1)
dPlotData <- data.frame(degree, reactomeDistributionScaled, stringsAsFactors = F)
dPlotData <- dPlotData[dPlotData$degree > 0 & dPlotData$reactomeDistributionScaled > 0, ]

print(paste(Sys.time(), " Plotting degree distribution", sep = ""))

dPlot <- ggplot()
dPlot <- dPlot + geom_point(data = dPlotData, aes(x = degree, y = reactomeDistributionScaled), col = "black", alpha = 0.3, size = 3)
dPlot <- dPlot + theme_bw(base_size = 22)
dPlot <- dPlot + scale_color_manual(name = element_blank(), values = colors)
dPlot <- dPlot + scale_x_log10(name = "degree", limits = c(1, 5000))
dPlot <- dPlot + scale_y_log10(name = "# Proteins", limits = c(1, 1000))
dPlot <- dPlot + theme(legend.position = 'none',
                       plot.title = element_text(hjust = 1))


png(paste("resources/iGraph/plots/timeline/poi_", year, ".png", sep = ""), width = 600, height = 800)
plot(dPlot)
dummy <- dev.off()




