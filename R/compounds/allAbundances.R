reactome <- read.table("../reactome.list", stringsAsFactors=FALSE)$V1

abundances <- read.table("../../resources/tissues/protein_MS.gz",
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

## plot density of abundances
## Reactome slightly to the right
pdf('proteinAbundanceVSReactome.pdf', width=13, height=7)
plot(density(abundances$intensity_average),
     xlab='Protein Abundance', ylab='Density',
     main='')
lines(density(abundances[abundances$accession %in% reactome,
                         "intensity_average"]),
      col = 'red')
legend('topleft', legend=c('All proteins', 'Reactome'),
       lty=1, col=c('black', 'red'))
dev.off()

## most compounds are pretty close to the average abundance density
## notable deviations are blood, bllod paltelets, and a couple of others
plot(density(abundances$intensity_average),
     xlab='Protein Abundance', ylab='Density',
     main='',
     xlim = c(0, 11), ylim = c(0, 0.8))
for (tis in unique(abundances$tissue)) {
    lines(density(abundances[abundances$tissue == tis,
                             "intensity_average"]),
          col = rgb(1,0,1,0.3))
}


pdf('abundancePerCompound.pdf', width=13, height=7)
for (tis in unique(abundances$tissue)) {
    plot(density(abundances$intensity_average),
         xlab='Protein Abundance', ylab='Density',
         main=tis,
         xlim = c(0, 11), ylim = c(0, 0.8))
    lines(density(abundances[abundances$tissue == tis,
                             "intensity_average"]),
          col = 'magenta')
    legend('topleft', legend=c('All proteins', tis),
           lty=1, col=c('black', 'magenta'))
}
dev.off()

## Reactome follows quite closely, though structurally slightly to the right
## So Reactome has very slightly bias towards more abundant proteins?
## notable exception is blood, where Reactome is a bit to the left.
pdf('abundancePerCompoundVSReactome.pdf', width=13, height=7)
for (tis in unique(abundances$tissue)) {
    plot(density(abundances$intensity_average),
         xlab='Protein Abundance', ylab='Density',
         main=tis,
         xlim = c(0, 11), ylim = c(0, 0.8))
    lines(density(abundances[abundances$tissue == tis,
                             "intensity_average"]),
          col = 'magenta')
    lines(density(abundances[abundances$tissue == tis &
                             abundances$accession %in% reactome,
                             "intensity_average"]),
          col = 'red')
    legend('topleft', legend=c('All proteins', tis, paste(tis, 'Reactome')),
           lty=1, col=c('black', 'magenta', 'red'))
}
dev.off()
