# 
# This script takes the iGraph data frames and plots them.
#
startTimeAll <- proc.time()


## Libraries

library(igraph)


## Parameters

alpha <- 0.2
defaultColor <- adjustcolor("grey60", alpha.f = alpha)

categoryLevels <- c("Reaction", "Catalysis", "Regulation", "Complex")
categoryColors <- c(defaultColor, 
                    adjustcolor("lightgreen", alpha.f = alpha), 
                    adjustcolor("lightblue", alpha.f = alpha), 
                    adjustcolor("orange", alpha.f = alpha))


## Main script

# Complexes

print(paste(Sys.time(), " Loading complexes data", sep = ""))

verticesComplexes <- read.table("resources/iGraph/complexes/complexes_18.08.17_vertices", header = T, sep = " ", stringsAsFactors = F)
edgesComplexes <- read.table("resources/iGraph/complexes/complexes_18.08.17_edges", header = T, sep = " ", stringsAsFactors = F)

print(paste(Sys.time(), " Making graph", sep = ""))

complexes <- graph_from_data_frame(d = edgesComplexes, vertices = verticesComplexes, directed = F)

print(paste(Sys.time(), " Plotting Network", sep = ""))

png("resources/iGraph/complexes.png", width = 800, height = 600)
l <- layout_with_fr(complexes)
plot(complexes, vertex.shape = "none", vertex.label = NA, edge.color = defaultColor, layout = l)
dev.off()


# Reactome

print(paste(Sys.time(), " Loading reactome data", sep = ""))

verticesReactome <- read.table("resources/iGraph/reactome/reactome_18.08.17_vertices", header = T, sep = " ", stringsAsFactors = F)
edgesReactome <- read.table("resources/iGraph/reactome/reactome_18.08.17_edges", header = T, sep = " ", stringsAsFactors = F)

print(paste(Sys.time(), " Making graph", sep = ""))

reactome <- graph_from_data_frame(d=edgesReactome, vertices=verticesReactome, directed=T)

print(paste(Sys.time(), " Formatting graph", sep = ""))

edgeColors <- factor(E(reactome)$type, levels = categoryLevels)
levels(edgeColors) <- categoryColors
edgeColors <- as.character(edgeColors)

print(paste(Sys.time(), " Plotting Network", sep = ""))

png("resources/iGraph/reactome.png", width = 800, height = 600)
plot(reactome, vertex.shape = "none", vertex.label = NA, edge.arrow.size = 0.2, edge.arrow.width = 0.2, edge.color = edgeColors)
dev.off()


# Reactome reactions only

print(paste(Sys.time(), " Loading reactions only", sep = ""))

edgesReactions <- edgesReactome[edgesReactome$type == "Reaction", c("from", "to")]
verticesReactions <- verticesReactome[verticesReactome$id %in% edgesReactions$from | verticesReactome$id %in% edgesReactions$to, ]

print(paste(Sys.time(), " Making graph", sep = ""))

reactomeReactions <- graph_from_data_frame(d=edgesReactions, vertices=verticesReactions, directed=T)

print(paste(Sys.time(), " Plotting Network", sep = ""))

png("resources/iGraph/reactome_reactions.png", width = 800, height = 600)
plot(reactome, vertex.shape = "none", vertex.label = NA, edge.arrow.size = 0.2, edge.arrow.width = 0.2, edge.color = defaultColor)
dev.off()


# Intact

print(paste(Sys.time(), " Loading Intact data", sep = ""))

verticesIntact <- read.table("resources/iGraph/intact/intact_18.08.17_vertices", header = T, sep = " ", stringsAsFactors = F)
edgesIntact <- read.table("resources/iGraph/intact/intact_18.08.17_edges", header = T, sep = " ", stringsAsFactors = F)

print(paste(Sys.time(), " Making graph", sep = ""))

intact <- graph_from_data_frame(d = edgesIntact, vertices = verticesIntact, directed = F)

print(paste(Sys.time(), " Plotting Network", sep = ""))

png("resources/iGraph/intact.png", width = 800, height = 600)
plot(intact, vertex.shape = "none", vertex.label = NA, edge.color = defaultColor)
dev.off()


# Interractome Mann

print(paste(Sys.time(), " Loading Mann data", sep = ""))

verticesMann <- read.table("resources/iGraph/intact/26496610_mann_vertices", header = T, sep = " ", stringsAsFactors = F)
edgesMann <- read.table("resources/iGraph/intact/26496610_mann_edges", header = T, sep = " ", stringsAsFactors = F)

print(paste(Sys.time(), " Making graph", sep = ""))

mann <- graph_from_data_frame(d = edgesMann, vertices = verticesMann, directed = F)

print(paste(Sys.time(), " Plotting Network", sep = ""))

png("resources/iGraph/mann.png", width = 800, height = 600)
plot(mann, vertex.shape = "none", vertex.label = NA, edge.color = defaultColor)
dev.off()




## End of process

endTimeAll <- proc.time()
diff <- endTimeAll - startTimeAll
diffMin <- round(diff[3]/60)
print(paste(Sys.time(), " Process completed (", diffMin, " min)", sep = ""))



