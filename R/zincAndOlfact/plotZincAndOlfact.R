zinc <- read.table("zinc.list",
                  stringsAsFactors=FALSE)$V1

verticesReactome <-
    read.table("../../resources/iGraph/reactome/reactome_18.08.17_vertices",
               header = T, sep = " ", stringsAsFactors = F)
edgesReactome <-
    read.table("../../resources/iGraph/reactome/reactome_18.08.17_edges",
               header = T, sep = " ", stringsAsFactors = F)

edgesZinc <- edgesReactome[edgesReactome$from %in% zinc | edgesReactome$to %in% zinc, ]
verticesZinc <- verticesReactome[verticesReactome$id %in% union(edgesReactome$to, edgesReactome$from), ]

edgesZinc$type <- ifelse(edgesZinc$from %in% zinc & edgesZinc$to %in% zinc,
                             "Zinc", "Other")

plot(density(sapply(verticesZinc, function (x) sum(edgesZinc$from == x))))
lines(density(sapply(verticesZinc, function (x) sum(edgesZinc[edgesZinc$type == "Zinc", ]$from == x))), col="blue")
lines(density(sapply(verticesZinc, function (x) sum(edgesZinc[edgesZinc$type == "Other", ]$from == x))), col="red")

nrow(edgesZinc)
length(verticesZinc)

zincLevels <- c("Zinc", "Other")

zinc <- graph_from_data_frame(d=edgesZinc, vertices=verticesZinc, directed=T)

edgeColors <- factor(E(zinc)$type, levels = zincLevels)
levels(edgeColors) <- categoryColors
edgeColors <- as.character(edgeColors)
alpha <- 0.2
categoryColors <- c(defaultColor,
                    adjustcolor("darkgreen", alpha.f = alpha),
                    adjustcolor("darkblue", alpha.f = alpha),
                    adjustcolor("orange", alpha.f = alpha))
png("ZincFingerAndFriends.png", width=800, height=600)
plot(zinc, vertex.shape = "none", vertex.label = NA,
     edge.arrow.size = 0, edge.arrow.width = 0, edge.color = edgeColors)
dev.off()




olf <- read.table("olfact.list",
                  stringsAsFactors=FALSE)$V1

edgesOlf <- edgesReactome[edgesReactome$from %in% olf | edgesReactome$to %in% olf, ]
verticesOlf <- verticesReactome[verticesReactome$id %in% union(edgesReactome$to, edgesReactome$from), ]

edgesOlf$type <- ifelse(edgesOlf$from %in% olf & edgesOlf$to %in% olf,
                             "Olf", "Other")

plot(density(sapply(verticesOlf, function (x) sum(edgesOlf$from == x))))
lines(density(sapply(verticesOlf, function (x) sum(edgesOlf[edgesOlf$type == "Olf", ]$from == x))), col="blue")
lines(density(sapply(verticesOlf, function (x) sum(edgesOlf[edgesOlf$type == "Other", ]$from == x))), col="red")

nrow(edgesOlf)
length(verticesOlf)

olfLevels <- c("Olf", "Other")

olf <- graph_from_data_frame(d=edgesOlf, vertices=verticesOlf, directed=T)

edgeColors <- factor(E(olf)$type, levels = olfLevels)
levels(edgeColors) <- categoryColors
edgeColors <- as.character(edgeColors)
alpha <- 0.2
categoryColors <- c(defaultColor,
                    adjustcolor("darkgreen", alpha.f = alpha),
                    adjustcolor("darkblue", alpha.f = alpha),
                    adjustcolor("orange", alpha.f = alpha))
png("OlfactoryAndFriends.png", width=800, height=600)
plot(olf, vertex.shape = "none", vertex.label = NA,
     edge.arrow.size = 0, edge.arrow.width = 0, edge.color = edgeColors)
dev.off()
