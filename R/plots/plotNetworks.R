# 
# This script takes the iGraph data frames and plots them.
#
startTimeAll <- proc.time()


## Libraries

library(igraph)


## Parameters

alpha <- 0.2
defaultColor <- adjustcolor("grey60", alpha.f = alpha)


## Main script

# Complexes

print(paste(Sys.time(), " Loading complexes data", sep = ""))

verticesComplexes <- read.table("resources/iGraph/complexes/complexes_18.08.17_vertices", header = T, sep = "\t", stringsAsFactors = F, quote = "", comment.char = "")
edgesComplexes <- read.table("resources/iGraph/complexes/complexes_18.08.17_edges", header = T, sep = " ", stringsAsFactors = F, quote = "", comment.char = "")

print(paste(Sys.time(), " Making graph", sep = ""))

complexes <- graph_from_data_frame(d = edgesComplexes, vertices = verticesComplexes, directed = F)

print(paste(Sys.time(), " Plotting Network", sep = ""))

png("resources/iGraph/plots/networks/complexes.png", width = 800, height = 600)
l <- layout_with_fr(complexes)
plot(complexes, vertex.shape = "none", vertex.label = NA, edge.color = defaultColor, layout = l)
dummy <- dev.off()


# Reactome

print(paste(Sys.time(), " Loading reactome data", sep = ""))

verticesReactome <- read.table("resources/iGraph/reactome/reactome_18.08.17_vertices", header = T, sep = "\t", stringsAsFactors = F, quote = "", comment.char = "")
edgesReactome <- read.table("resources/iGraph/reactome/reactome_18.08.17_edges", header = T, sep = " ", stringsAsFactors = F, quote = "", comment.char = "")

print(paste(Sys.time(), " Making graph", sep = ""))

reactome <- graph_from_data_frame(d=edgesReactome, vertices=verticesReactome, directed=T)

print(paste(Sys.time(), " Formatting graph", sep = ""))

reactomeCategoryLevels <- c("Reaction", "Catalysis", "Regulation", "Complex")
ReactomeCategoryColors <- c(defaultColor, 
                            adjustcolor("lightgreen", alpha.f = alpha), 
                            adjustcolor("lightblue", alpha.f = alpha), 
                            adjustcolor("orange", alpha.f = alpha))

edgeColors <- factor(E(reactome)$type, levels = reactomeCategoryLevels)
levels(edgeColors) <- ReactomeCategoryColors
edgeColors <- as.character(edgeColors)

print(paste(Sys.time(), " Plotting Network", sep = ""))

png("resources/iGraph/plots/networks/reactome.png", width = 800, height = 600)
plot(reactome, vertex.shape = "none", vertex.label = NA, edge.arrow.size = 0.2, edge.arrow.width = 0.2, edge.color = edgeColors)
dummy <- dev.off()


# Reactome reactions only

print(paste(Sys.time(), " Loading reactions only", sep = ""))

edgesReactions <- edgesReactome[edgesReactome$type == "Reaction", c("from", "to")]
verticesReactions <- verticesReactome[verticesReactome$id %in% edgesReactions$from | verticesReactome$id %in% edgesReactions$to, ]

print(paste(Sys.time(), " Making graph", sep = ""))

reactomeReactions <- graph_from_data_frame(d=edgesReactions, vertices=verticesReactions, directed=T)

print(paste(Sys.time(), " Plotting Network", sep = ""))

png("resources/iGraph/plots/networks/reactome_reactions.png", width = 800, height = 600)
plot(reactome, vertex.shape = "none", vertex.label = NA, edge.arrow.size = 0.2, edge.arrow.width = 0.2, edge.color = defaultColor)
dummy <- dev.off()


# Kegg

print(paste(Sys.time(), " Loading KEGG data", sep = ""))

verticesKegg <- read.table("resources/iGraph/kegg/kegg_21.08.17_vertices", header = T, sep = "\t", stringsAsFactors = F, quote = "", comment.char = "")
edgesKegg <- read.table("resources/iGraph/kegg/kegg_21.08.17_edges", header = T, sep = " ", stringsAsFactors = F, quote = "", comment.char = "")

print(paste(Sys.time(), " Making graph", sep = ""))

kegg <- graph_from_data_frame(d = edgesKegg, vertices = verticesKegg, directed = F)

print(paste(Sys.time(), " Formatting graph", sep = ""))

edgeColors <- factor(E(kegg)$type)
levels(edgeColors) <- adjustcolor(scales::hue_pal()(length(levels(edgeColors))), alpha.f = alpha)
edgeColors <- as.character(edgeColors)

print(paste(Sys.time(), " Plotting Network", sep = ""))

png("resources/iGraph/plots/networks/kegg.png", width = 800, height = 600)
plot(reactome, vertex.shape = "none", vertex.label = NA, edge.arrow.size = 0.2, edge.arrow.width = 0.2, edge.color = edgeColors)
dummy <- dev.off()


# Intact

print(paste(Sys.time(), " Loading Intact data", sep = ""))

verticesIntact <- read.table("resources/iGraph/intact/intact_18.08.17_vertices", header = T, sep = "\t", stringsAsFactors = F, quote = "", comment.char = "")
edgesIntact <- read.table("resources/iGraph/intact/intact_18.08.17_edges", header = T, sep = " ", stringsAsFactors = F, quote = "", comment.char = "")

print(paste(Sys.time(), " Making graph", sep = ""))

intact <- graph_from_data_frame(d = edgesIntact, vertices = verticesIntact, directed = F)

print(paste(Sys.time(), " Plotting Network", sep = ""))

png("resources/iGraph/plots/networks/intact.png", width = 800, height = 600)
plot(intact, vertex.shape = "none", vertex.label = NA, edge.color = defaultColor)
dummy <- dev.off()


# Interractome Mann

print(paste(Sys.time(), " Loading Mann data", sep = ""))

verticesMann <- read.table("resources/iGraph/intact/26496610_mann_vertices", header = T, sep = "\t", stringsAsFactors = F, quote = "", comment.char = "")
edgesMann <- read.table("resources/iGraph/intact/26496610_mann_edges", header = T, sep = " ", stringsAsFactors = F, quote = "", comment.char = "")

print(paste(Sys.time(), " Making graph", sep = ""))

mann <- graph_from_data_frame(d = edgesMann, vertices = verticesMann, directed = F)

print(paste(Sys.time(), " Plotting Network", sep = ""))

png("resources/iGraph/plots/networks/mann.png", width = 800, height = 600)
plot(mann, vertex.shape = "none", vertex.label = NA, edge.color = defaultColor)
dummy <- dev.off()


# Biogrid

print(paste(Sys.time(), " Loading Biogrid data", sep = ""))

verticesBiogrid <- read.table("resources/iGraph/biogrid/BIOGRID-ORGANISM-Homo_sapiens-3.4.151_vertices", header = T, sep = "\t", stringsAsFactors = F, quote = "", comment.char = "")
edgesBiogrid <- read.table("resources/iGraph/biogrid/BIOGRID-ORGANISM-Homo_sapiens-3.4.151_edges", header = T, sep = " ", stringsAsFactors = F, quote = "", comment.char = "")

print(paste(Sys.time(), " Making graph", sep = ""))

biogrid <- graph_from_data_frame(d = edgesBiogrid, vertices = verticesBiogrid, directed = F)

print(paste(Sys.time(), " Plotting Network", sep = ""))

png("resources/iGraph/plots/networks/biogrid.png", width = 800, height = 600)
plot(biogrid, vertex.shape = "none", vertex.label = NA, edge.color = defaultColor)
dummy <- dev.off()


# Gygi

print(paste(Sys.time(), " Loading Gygi data", sep = ""))

verticesGygi <- read.table("resources/iGraph/biogrid/28514442_vertices", header = T, sep = "\t", stringsAsFactors = F, quote = "", comment.char = "")
edgesGygi <- read.table("resources/iGraph/biogrid/28514442_edges", header = T, sep = " ", stringsAsFactors = F, quote = "", comment.char = "")

print(paste(Sys.time(), " Making graph", sep = ""))

gygi <- graph_from_data_frame(d = edgesBiogrid, vertices = verticesBiogrid, directed = F)

print(paste(Sys.time(), " Plotting Network", sep = ""))

png("resources/iGraph/plots/networks/gygi.png", width = 800, height = 600)
plot(gygi, vertex.shape = "none", vertex.label = NA, edge.color = defaultColor)
dummy <- dev.off()


# String

print(paste(Sys.time(), " Loading String data", sep = ""))

verticesString <- read.table("resources/iGraph/string/string_v10.5_vertices", header = T, sep = "\t", stringsAsFactors = F, quote = "", comment.char = "")
edgesString <- read.table("resources/iGraph/string/string_v10.5_edges", header = T, sep = " ", stringsAsFactors = F, quote = "", comment.char = "")

print(paste(Sys.time(), " Making graph", sep = ""))

string <- graph_from_data_frame(d = edgesBiogrid, vertices = verticesBiogrid, directed = F)

print(paste(Sys.time(), " Plotting Network", sep = ""))

png("resources/iGraph/plots/networks/string.png", width = 800, height = 600)
plot(string, vertex.shape = "none", vertex.label = NA, edge.color = defaultColor)
dummy <- dev.off()



## End of process

endTimeAll <- proc.time()
diff <- endTimeAll - startTimeAll
diffMin <- round(diff[3]/60)
print(paste(Sys.time(), " Process completed (", diffMin, " min)", sep = ""))



