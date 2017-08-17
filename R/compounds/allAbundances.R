reactome <- read.table("../reactome.list", stringsAsFactors=FALSE)$V1

abundances <- read.table("../../resources/tissues/protein_MS/blood.platelet.gz",
                         header=TRUE, sep='\t', stringsAsFactors=FALSE)

nrow(abundances)
ncol(abundances)
colnames(abundances)
unique(abundances$category)
plot(density(abundances$intensity_average))
lines(density(abundances$intensity_raw), col='pink')
lines(density(abundances$intensity_max), col='red')
lines(density(abundances$intensity_min), col='blue')

## density of the range of intensity
plot(density(with(abundances, intensity_max - intensity_min)))

head(abundances$accession)
sum(abundances$accession %in% reactome)
sum(! abundances$accession %in% reactome)

plot(density(abundances$intensity_average))
lines(density(abundances[abundances$accession %in% reactome,
                         "intensity_average"]))
