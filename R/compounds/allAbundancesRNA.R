reactome <- read.table("../reactome.list", stringsAsFactors=FALSE)$V1

abundancesRNA <- read.table("../../resources/tissues/RNA.gz",
                            colClasses=c(rep("character", 4),
                                         "numeric"),
                            quote="",
                            header=TRUE, sep='\t', stringsAsFactors=FALSE)

plot(abundancesRNA$tpm)
summary(abundancesRNA$tpm)

plot(density(abundancesRNA$tpm, na.rm=TRUE), xlim=c(0,2000))
lines(density(abundancesRNA[abundancesRNA$accession %in% reactome,
                            "tpm"], na.rm=TRUE),
      col= 'red')


pdf('RNAlogAbundanceVSReactome.pdf', width=13, height=7)
plot(density(log(abundancesRNA$tpm), na.rm=TRUE),
     xlab='Protein Abundance', ylab='Density',
     main="", ylim=c(0,0.35))
lines(density(log(abundancesRNA[abundancesRNA$accession %in% reactome,
                                "tpm"]), na.rm=TRUE),
      col = 'red')
legend('topleft', legend=c('All proteins', 'Reactome'),
       lty=1, col=c('black', 'red'))
dev.off()

pdf('RNAlogAbundancePerCompoundOnePlot.pdf', width=13, height=7)
plot(density(log(abundancesRNA$tpm), na.rm=TRUE),
     xlab='Protein Abundance', ylab='Density',
     main="", ylim=c(0,0.35))
for (tis in unique(abundancesRNA$tissue)) {
    if (sum(abundancesRNA$tissue == tis, na.rm=TRUE) > 2)
        lines(density(log(abundancesRNA[abundancesRNA$tissue == tis,
                                        "tpm"]), na.rm=TRUE),
              col = rgb(1,0,1,0.2))
}
dev.off()

pdf('RNAlogAbundancePerCompoundVSReactome.pdf', width=13, height=7)
for (tis in unique(abundancesRNA$tissue)) {
    if (sum(abundancesRNA$tissue == tis, na.rm=TRUE) > 2) {
        plot(density(log(abundancesRNA$tpm), na.rm=TRUE),
             xlab='Protein Abundance RNA', ylab='Density',
             main=tis,
             ylim = c(0, 0.35))
        lines(density(log(abundancesRNA[abundancesRNA$tissue == tis,
                                        "tpm"]), na.rm=TRUE),
              col = 'magenta')
        lines(density(log(abundancesRNA[abundancesRNA$tissue == tis &
                                        abundancesRNA$accession %in% reactome,
                                        "tpm"]), na.rm=TRUE),
              col = 'red')
        legend('topright', legend=c('All proteins', tis, paste(tis, 'Reactome')),
               lty=1, col=c('black', 'magenta', 'red'))
    }
}
dev.off()

