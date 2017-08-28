# 
# This script takes the iGraph data frames from various functional resources and plots various size metrics.
#
startTimeAll <- proc.time()



## Libraries

library(igraph)
library(ggplot2)
library(ggrepel)



## Parameters

mainCategoryNames <- c("Complex", "Pathway", "Interaction", "Composite")
categoryNames <- c("Complexes", "Reactome", "Reactions", "Kegg", "Biogrid", "Intact", "Huttlin", "Hein", "String")
categoryColors <- c("#e31a1c", "#1f78b4", "#a6cee3", "#33a02c", "#ff7f00", "#6a3d9a", "#fdbf6f", "#cab2d6", "#999999")


## Main script


# Load data

print(paste(Sys.time(), " Loading data", sep = ""))

verticesComplexes <- read.table("resources/iGraph/complexes/complexes_18.08.17_vertices", header = T, sep = "\t", stringsAsFactors = F, quote = "", comment.char = "")
edgesComplexes <- read.table("resources/iGraph/complexes/complexes_18.08.17_edges", header = T, sep = " ", stringsAsFactors = F, quote = "", comment.char = "")

verticesReactome <- read.table("resources/iGraph/reactome/reactome_18.08.17_vertices", header = T, sep = "\t", stringsAsFactors = F, quote = "", comment.char = "")
edgesReactome <- read.table("resources/iGraph/reactome/reactome_18.08.17_edges", header = T, sep = " ", stringsAsFactors = F, quote = "", comment.char = "")

edgesReactions <- edgesReactome[edgesReactome$type == "Reaction", c("from", "to")]
verticesReactions <- verticesReactome[verticesReactome$id %in% edgesReactions$from | verticesReactome$id %in% edgesReactions$to, ]

verticesKegg <- read.table("resources/iGraph/kegg/kegg_21.08.17_vertices", header = T, sep = "\t", stringsAsFactors = F, quote = "", comment.char = "")
edgesKegg <- read.table("resources/iGraph/kegg/kegg_21.08.17_edges", header = T, sep = " ", stringsAsFactors = F, quote = "", comment.char = "")

verticesBiogrid <- read.table("resources/iGraph/biogrid/BIOGRID-ORGANISM-Homo_sapiens-3.4.151_vertices", header = T, sep = "\t", stringsAsFactors = F, quote = "", comment.char = "")
edgesBiogrid <- read.table("resources/iGraph/biogrid/BIOGRID-ORGANISM-Homo_sapiens-3.4.151_edges", header = T, sep = " ", stringsAsFactors = F, quote = "", comment.char = "")

verticesIntact <- read.table("resources/iGraph/intact/intact_18.08.17_vertices", header = T, sep = "\t", stringsAsFactors = F, quote = "", comment.char = "")
edgesIntact <- read.table("resources/iGraph/intact/intact_18.08.17_edges", header = T, sep = " ", stringsAsFactors = F, quote = "", comment.char = "")

verticesGygi <- read.table("resources/iGraph/biogrid/28514442_vertices", header = T, sep = "\t", stringsAsFactors = F, quote = "", comment.char = "")
edgesGygi <- read.table("resources/iGraph/biogrid/28514442_edges", header = T, sep = " ", stringsAsFactors = F, quote = "", comment.char = "")

verticesMann <- read.table("resources/iGraph/intact/26496610_mann_vertices", header = T, sep = "\t", stringsAsFactors = F, quote = "", comment.char = "")
edgesMann <- read.table("resources/iGraph/intact/26496610_mann_edges", header = T, sep = " ", stringsAsFactors = F, quote = "", comment.char = "")

verticesString <- read.table("resources/iGraph/string/string_v10.5_medium_vertices", header = T, sep = "\t", stringsAsFactors = F, quote = "", comment.char = "")
edgesString <- read.table("resources/iGraph/string/string_v10.5_medium_edges", header = T, sep = " ", stringsAsFactors = F, quote = "", comment.char = "")


# Get graphs and degrees

print(paste(Sys.time(), " Processing graphs", sep = ""))

complexes <- graph_from_data_frame(d = edgesComplexes, vertices = verticesComplexes, directed = F)
reactome <- graph_from_data_frame(d=edgesReactome, vertices=verticesReactome, directed=F)
reactomeReactions <- graph_from_data_frame(d=edgesReactions, vertices=verticesReactions, directed=F)
kegg <- graph_from_data_frame(d = edgesKegg, vertices = verticesKegg, directed = F)
biogrid <- graph_from_data_frame(d = edgesBiogrid, vertices = verticesBiogrid, directed = F)
intact <- graph_from_data_frame(d = edgesIntact, vertices = verticesIntact, directed = F)
gygi <- graph_from_data_frame(d = edgesGygi, vertices = verticesGygi, directed = F)
mann <- graph_from_data_frame(d = edgesMann, vertices = verticesMann, directed = F)
string <- graph_from_data_frame(d = edgesString, vertices = verticesString, directed = F)


# Gather size information

print(paste(Sys.time(), " Gathering size information", sep = ""))

sizeDataFrame <- data.frame(categoryNames, categoryColors, stringsAsFactors = F)
sizeDataFrame$category <- factor(sizeDataFrame$categoryNames, levels = categoryNames)

# number of vertices

sizeDataFrame$nVertices <- c(
  nrow(verticesComplexes),
  nrow(verticesReactome),
  nrow(verticesReactions),
  nrow(verticesKegg),
  nrow(verticesBiogrid),
  nrow(verticesIntact),
  nrow(verticesGygi),
  nrow(verticesMann),
  nrow(verticesString)
)

# number of edges

sizeDataFrame$nEdges <- c(
  nrow(edgesComplexes),
  nrow(edgesReactome),
  nrow(edgesReactions),
  nrow(edgesKegg),
  nrow(edgesBiogrid),
  nrow(edgesIntact),
  nrow(edgesGygi),
  nrow(edgesMann),
  nrow(edgesString)
)

# number of clusters

sizeDataFrame$nClusters <- c(
  length(groups(components(complexes))),
  length(groups(components(reactome))),
  length(groups(components(reactomeReactions))),
  length(groups(components(kegg))),
  length(groups(components(biogrid))),
  length(groups(components(intact))),
  length(groups(components(gygi))),
  length(groups(components(mann))),
  length(groups(components(string)))
)

# number of cliques

print(paste(Sys.time(), " Clique complexes", sep = ""))
cnComplexes <- clique.number(complexes)

print(paste(Sys.time(), " Clique Reactome", sep = ""))
cdReactome <- clique.number(reactome)

print(paste(Sys.time(), " Clique reactions", sep = ""))
cdReactions <- 0 # clique.number(reactomeReactions)

print(paste(Sys.time(), " Clique Kegg", sep = ""))
cdKegg <- clique.number(kegg)

print(paste(Sys.time(), " Clique Biogrid", sep = ""))
cdBiogrid <- clique.number(biogrid)

print(paste(Sys.time(), " Clique Intact", sep = ""))
cdIntact <- clique.number(intact)

print(paste(Sys.time(), " Clique Gygi", sep = ""))
cdGygi <- 0 # clique.number(gygi)

print(paste(Sys.time(), " Clique Mann", sep = ""))
cdMann <- 0 # clique.number(mann)

print(paste(Sys.time(), " Clique String", sep = ""))
cdString <- clique.number(string)

sizeDataFrame$nCliques <- c(
  cnComplexes,
  cdReactome,
  cdReactions,
  cdKegg,
  cdBiogrid,
  cdIntact,
  cdGygi,
  cdMann,
  cdString
)

# diameter

sizeDataFrame$diameter <- c(
  diameter(complexes),
  diameter(reactome),
  diameter(reactomeReactions),
  diameter(kegg),
  diameter(biogrid),
  diameter(intact),
  diameter(gygi),
  diameter(mann),
  diameter(string)
)

write.table(sizeDataFrame, "resources/iGraph/plots/size/sizeDataFrame", col.names = T, row.names = T, quote = F)

trimmedDataFrame <- sizeDataFrame[sizeDataFrame$categoryNames %in% c("Complexes", "Reactome", "Kegg", "Biogrid", "Intact", "String"), ]


# Plotting size information

print(paste(Sys.time(), " Exporting plots", sep = ""))

evPlot <- ggplot()
evPlot <- evPlot + geom_point(data = trimmedDataFrame, aes(x = nVertices, y = nEdges, col = category), size = 8)
evPlot <- evPlot + geom_vline(aes(xintercept = 20201), col = "green", linetype = "dashed")
evPlot <- evPlot + theme_bw(base_size = 33)
evPlot <- evPlot + scale_color_manual(name = element_blank(), values = trimmedDataFrame$categoryColors)
evPlot <- evPlot + scale_x_log10(name = "# Proteins")
evPlot <- evPlot + scale_y_log10(name = "# Links")
evPlot <- evPlot + theme(legend.position = 'none')
evPlot <- evPlot + geom_text_repel(data = trimmedDataFrame, aes(x = nVertices, y = nEdges, label = categoryNames), point.padding = unit(1.6, 'lines'), size = 8)


png("resources/iGraph/plots/size/edges-vertices.png", width = 800, height = 600)
plot(evPlot)
dummy <- dev.off()


# Plotting size information

print(paste(Sys.time(), " Exporting plots", sep = ""))

cdPlot <- ggplot()
cdPlot <- cdPlot + geom_point(data = trimmedDataFrame, aes(x = nClusters, y = diameter, col = category), size = 8)
cdPlot <- cdPlot + theme_bw(base_size = 33)
cdPlot <- cdPlot + scale_color_manual(name = element_blank(), values = trimmedDataFrame$categoryColors)
cdPlot <- cdPlot + scale_x_log10(name = "# clusters", breaks = c(50, 100, 200, 1000))
cdPlot <- cdPlot + scale_y_log10(name = "# diameter", breaks = c(8, 10, 12, 15, 20))
cdPlot <- cdPlot + theme(legend.position = 'none')
cdPlot <- cdPlot + geom_text_repel(data = trimmedDataFrame, aes(x = nClusters, y = diameter, label = categoryNames), point.padding = unit(1.6, 'lines'), size = 8)
cdPlot <- cdPlot + theme(panel.grid.minor = element_blank())


png("resources/iGraph/plots/size/cluster-diameter.png", width = 800, height = 600)
plot(cdPlot)
dummy <- dev.off()

# Make adjacency matrices

complexes <- simplify(complexes, remove.multiple = T, remove.loops = T) 
reactome <- simplify(reactome, remove.multiple = T, remove.loops = T) 
kegg <- simplify(kegg, remove.multiple = T, remove.loops = T) 
biogrid <- simplify(biogrid, remove.multiple = T, remove.loops = T) 
intact <- simplify(intact, remove.multiple = T, remove.loops = T) 
gygi <- simplify(gygi, remove.multiple = T, remove.loops = T) 
mann <- simplify(mann, remove.multiple = T, remove.loops = T) 
string <- simplify(string, remove.multiple = T, remove.loops = T) 


verticesComplexes$comm <- membership(cluster_fast_greedy(complexes))
verticesComplexes <- verticesComplexes[order(verticesComplexes$comm, verticesComplexes$id), ]

verticesReactome$comm <- membership(cluster_fast_greedy(reactome))
verticesReactome <- verticesReactome[order(verticesReactome$comm, verticesReactome$id), ]

verticesKegg$comm <- membership(cluster_fast_greedy(kegg))
verticesKegg <- verticesKegg[order(verticesKegg$comm, verticesKegg$id), ]

verticesBiogrid$comm <- membership(cluster_fast_greedy(biogrid))
verticesBiogrid <- verticesBiogrid[order(verticesBiogrid$comm, verticesBiogrid$id), ]

verticesIntact$comm <- membership(cluster_fast_greedy(intact))
verticesIntact <- verticesIntact[order(verticesIntact$comm, verticesIntact$id), ]

verticesGygi$comm <- membership(cluster_fast_greedy(gygi))
verticesGygi <- verticesGygi[order(verticesGygi$comm, verticesGygi$id), ]

verticesMann$comm <- membership(cluster_fast_greedy(mann))
verticesMann <- verticesMann[order(verticesMann$comm, verticesMann$id), ]

verticesString$comm <- membership(cluster_fast_greedy(string))
verticesString <- verticesString[order(verticesString$comm, verticesString$id), ]

complexesDataFrame <- edgesComplexes %>% mutate(
  to = factor(to, levels = verticesComplexes$id),
  from = factor(from, levels = verticesComplexes$id))
complexesDataFrame$typeFactor <- as.factor(complexesDataFrame$type)

reactomeDataFrame <- edgesReactome %>% mutate(
  to = factor(to, levels = verticesReactome$id),
  from = factor(from, levels = verticesReactome$id))
reactomeDataFrame$typeFactor <- as.factor(reactomeDataFrame$type)

keggDataFrame <- edgesKegg %>% mutate(
  to = factor(to, levels = verticesKegg$id),
  from = factor(from, levels = verticesKegg$id))
keggDataFrame$typeFactor <- as.factor(keggDataFrame$type)

biogridDataFrame <- edgesBiogrid %>% mutate(
  to = factor(to, levels = verticesBiogrid$id),
  from = factor(from, levels = verticesBiogrid$id))
biogridDataFrame$typeFactor <- as.factor(biogridDataFrame$type)

intactDataFrame <- edgesIntact %>% mutate(
  to = factor(to, levels = verticesIntact$id),
  from = factor(from, levels = verticesIntact$id))
intactDataFrame$typeFactor <- as.factor(intactDataFrame$type)

gygiDataFrame <- edgesGygi %>% mutate(
  to = factor(to, levels = verticesGygi$id),
  from = factor(from, levels = verticesGygi$id))
gygiDataFrame$typeFactor <- as.factor(gygiDataFrame$type)

mannDataFrame <- edgesMann %>% mutate(
  to = factor(to, levels = verticesMann$id),
  from = factor(from, levels = verticesMann$id))
mannDataFrame$typeFactor <- as.factor(mannDataFrame$type)

stringDataFrame <- edgesString %>% mutate(
  to = factor(to, levels = verticesString$id),
  from = factor(from, levels = verticesString$id))
stringDataFrame$typeFactor <- as.factor(stringDataFrame$type)


heatMapPlot <- ggplot()
heatMapPlot <- heatMapPlot + theme_bw()
heatMapPlot <- heatMapPlot + geom_tile(data = complexesDataFrame, aes(x = from, y = to, fill = typeFactor))
heatMapPlot <- heatMapPlot + scale_x_discrete(name = element_blank(), expand = c(0, 0))
heatMapPlot <- heatMapPlot + scale_y_discrete(name = element_blank(), position = "right", expand = c(0, 0))
heatMapPlot <- heatMapPlot + scale_fill_manual(values = c("black"))
heatMapPlot <- heatMapPlot + theme(axis.title = element_blank(),
                                   axis.text = element_blank(),
                                   axis.ticks = element_blank(),
                                   panel.grid = element_blank(),
                                   legend.position = 'none')

png("resources/iGraph/plots/size/complexes.png", width = 3200, height = 2400)
plot(heatMapPlot)
dummy <- dev.off()


heatMapPlot <- ggplot()
heatMapPlot <- heatMapPlot + theme_bw()
heatMapPlot <- heatMapPlot + geom_tile(data = reactomeDataFrame, aes(x = from, y = to, fill = typeFactor))
heatMapPlot <- heatMapPlot + scale_x_discrete(name = element_blank(), expand = c(0, 0))
heatMapPlot <- heatMapPlot + scale_y_discrete(name = element_blank(), position = "right", expand = c(0, 0))
heatMapPlot <- heatMapPlot + scale_fill_manual(values = c("darkgreen", "black", "blue", "darkred"))
heatMapPlot <- heatMapPlot + theme(axis.title = element_blank(),
                                   axis.text = element_blank(),
                                   axis.ticks = element_blank(),
                                   panel.grid = element_blank(),
                                   legend.position = 'none')

png("resources/iGraph/plots/size/reactome.png", width = 3200, height = 2400)
plot(heatMapPlot)
dummy <- dev.off()


heatMapPlot <- ggplot()
heatMapPlot <- heatMapPlot + theme_bw()
heatMapPlot <- heatMapPlot + geom_tile(data = keggDataFrame, aes(x = from, y = to), fill = "black")
heatMapPlot <- heatMapPlot + scale_x_discrete(name = element_blank(), expand = c(0, 0))
heatMapPlot <- heatMapPlot + scale_y_discrete(name = element_blank(), position = "right", expand = c(0, 0))
heatMapPlot <- heatMapPlot + theme(axis.title = element_blank(),
                                   axis.text = element_blank(),
                                   axis.ticks = element_blank(),
                                   panel.grid = element_blank(),
                                   legend.position = 'none')

png("resources/iGraph/plots/size/kegg_bw.png", width = 3200, height = 2400)
plot(heatMapPlot)
dummy <- dev.off()


heatMapPlot <- ggplot()
heatMapPlot <- heatMapPlot + theme_bw()
heatMapPlot <- heatMapPlot + geom_tile(data = biogridDataFrame, aes(x = from, y = to), fill = "black")
heatMapPlot <- heatMapPlot + scale_x_discrete(name = element_blank(), expand = c(0, 0))
heatMapPlot <- heatMapPlot + scale_y_discrete(name = element_blank(), position = "right", expand = c(0, 0))
heatMapPlot <- heatMapPlot + theme(axis.title = element_blank(),
                                   axis.text = element_blank(),
                                   axis.ticks = element_blank(),
                                   panel.grid = element_blank(),
                                   legend.position = 'none')

png("resources/iGraph/plots/size/biogrid.png", width = 6400, height = 4800)
plot(heatMapPlot)
dummy <- dev.off()


heatMapPlot <- ggplot()
heatMapPlot <- heatMapPlot + theme_bw()
heatMapPlot <- heatMapPlot + geom_tile(data = intactDataFrame, aes(x = from, y = to), fill = "black")
heatMapPlot <- heatMapPlot + scale_x_discrete(name = element_blank(), expand = c(0, 0))
heatMapPlot <- heatMapPlot + scale_y_discrete(name = element_blank(), position = "right", expand = c(0, 0))
heatMapPlot <- heatMapPlot + theme(axis.title = element_blank(),
                                   axis.text = element_blank(),
                                   axis.ticks = element_blank(),
                                   panel.grid = element_blank(),
                                   legend.position = 'none')

png("resources/iGraph/plots/size/intact.png", width = 6400, height = 4800)
plot(heatMapPlot)
dummy <- dev.off()


heatMapPlot <- ggplot()
heatMapPlot <- heatMapPlot + theme_bw()
heatMapPlot <- heatMapPlot + geom_tile(data = gygiDataFrame, aes(x = from, y = to, fill = typeFactor))
heatMapPlot <- heatMapPlot + scale_x_discrete(name = element_blank(), expand = c(0, 0))
heatMapPlot <- heatMapPlot + scale_y_discrete(name = element_blank(), position = "right", expand = c(0, 0))
heatMapPlot <- heatMapPlot + scale_fill_manual(values = c("black"))
heatMapPlot <- heatMapPlot + theme(axis.title = element_blank(),
                                   axis.text = element_blank(),
                                   axis.ticks = element_blank(),
                                   panel.grid = element_blank(),
                                   legend.position = 'none')

png("resources/iGraph/plots/size/gygi.png", width = 3200, height = 2400)
plot(heatMapPlot)
dummy <- dev.off()


heatMapPlot <- ggplot()
heatMapPlot <- heatMapPlot + theme_bw()
heatMapPlot <- heatMapPlot + geom_tile(data = mannDataFrame, aes(x = from, y = to, fill = typeFactor))
heatMapPlot <- heatMapPlot + scale_x_discrete(name = element_blank(), expand = c(0, 0))
heatMapPlot <- heatMapPlot + scale_y_discrete(name = element_blank(), position = "right", expand = c(0, 0))
heatMapPlot <- heatMapPlot + scale_fill_manual(values = c("black"))
heatMapPlot <- heatMapPlot + theme(axis.title = element_blank(),
                                   axis.text = element_blank(),
                                   axis.ticks = element_blank(),
                                   panel.grid = element_blank(),
                                   legend.position = 'none')

png("resources/iGraph/plots/size/mann.png", width = 3200, height = 2400)
plot(heatMapPlot)
dummy <- dev.off()


heatMapPlot <- ggplot()
heatMapPlot <- heatMapPlot + theme_bw(base_size = 22)
heatMapPlot <- heatMapPlot + geom_tile(data = stringDataFrame, aes(x = from, y = to), fill = "black")
heatMapPlot <- heatMapPlot + scale_x_discrete(name = element_blank(), expand = c(0, 0))
heatMapPlot <- heatMapPlot + scale_y_discrete(name = element_blank(), position = "right", expand = c(0, 0))
heatMapPlot <- heatMapPlot + theme(axis.title = element_blank(),
                                   axis.text = element_blank(),
                                   axis.ticks = element_blank(),
                                   panel.grid = element_blank(),
                                   legend.position = 'none')

png("resources/iGraph/plots/size/string.png", width = 3200, height = 2400)
plot(heatMapPlot)
dummy <- dev.off()



