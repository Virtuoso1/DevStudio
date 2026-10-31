package com.homeservices.app.data

/** Sample data for the front-end build. Replace with the real backend later. */
object ProData {
    val all: List<Pro> = listOf(
        Pro(
            "pro_peter", "Peter Mwangi", Trade.ELECTRICIAN, 4.78, 6, 214,
            "Certified electrician, 6 years on the job: sockets, wiring faults and full rewires. " +
                "Fixing electrical faults across Nairobi since 2019, from tripped breakers and sparking " +
                "sockets to full house rewiring for new tenants. Trained at Kenya Power's technician " +
                "program and holds a current electrical safety certification.",
            listOf("Kenya Power technician program", "Electrical safety certified", "Full rewiring"),
        ),
        Pro(
            "pro_sam", "Sam Mutuku", Trade.ELECTRICIAN, 4.65, 4, 132,
            "Installations, lighting and prepaid meter hookups for apartments and small offices. " +
                "Quick to respond when a fault is urgent.",
            listOf("Electrical safety certified", "Lighting installs"),
        ),
        Pro(
            "pro_matt", "Matt Too", Trade.ELECTRICIAN, 4.60, 8, 301,
            "Wiring and fault-finding specialist with 8 years in residential estates. " +
                "Handles distribution boards, earthing and socket upgrades.",
            listOf("Fault finding", "Distribution boards", "Earthing"),
        ),
        Pro(
            "pro_faith", "Faith Wambui", Trade.PLUMBER, 4.89, 9, 388,
            "Licensed plumber with 9 years fixing leaks, blocked drains and water heater installs. " +
                "Known for neat pipework and clear quotes before work starts.",
            listOf("Licensed plumber", "Water heater installs", "Leak detection"),
        ),
        Pro(
            "pro_harun", "Harun Aketch", Trade.PLUMBER, 4.82, 7, 256,
            "Bathroom and kitchen plumbing, from new fittings to emergency pipe repairs.",
            listOf("Licensed plumber", "Bathroom fit-outs"),
        ),
        Pro(
            "pro_john", "John Wambua", Trade.PLUMBER, 4.74, 5, 167,
            "Drainage, tanks and pump installations for homes and rental units.",
            listOf("Drainage", "Tank and pump installs"),
        ),
        Pro(
            "pro_cyd", "Cyd Njeri", Trade.PLUMBER, 4.67, 3, 94,
            "Reliable repairs for taps, toilets and showers, with same-day availability.",
            listOf("Same-day repairs", "Tap and shower fixes"),
        ),
        Pro(
            "pro_charles", "Charles Too", Trade.APPLIANCE, 4.71, 7, 242,
            "Appliance technician repairing fridges, washing machines and cookers. " +
                "Brand-trained on Samsung, LG and Hotpoint.",
            listOf("Brand-trained technician", "Fridge and freezer repair", "Washing machines"),
        ),
        Pro(
            "pro_thomas", "Thomas Ngeira", Trade.APPLIANCE, 4.68, 5, 149,
            "Cooker, oven and microwave repairs with a clear diagnosis before any parts are replaced.",
            listOf("Cookers and ovens", "Microwave repair"),
        ),
        Pro(
            "pro_ngatho", "Ngatho Joseph", Trade.APPLIANCE, 4.62, 4, 118,
            "Washing machine and dishwasher specialist covering Nairobi and surrounding estates.",
            listOf("Washing machines", "Dishwashers"),
        ),
    )

    val bookAgainIds = listOf("pro_peter", "pro_faith", "pro_charles")
    val bookAgain: List<Pro> get() = bookAgainIds.mapNotNull(::byId)

    fun byId(id: String): Pro? = all.firstOrNull { it.id == id }

    fun byTrade(trade: Trade): List<Pro> = all.filter { it.trade == trade }

    fun search(query: String): List<Pro> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return emptyList()
        return all.filter { pro ->
            pro.name.lowercase().contains(q) ||
                pro.trade.label.lowercase().contains(q) ||
                pro.trade.plural.lowercase().contains(q) ||
                pro.bio.lowercase().contains(q) ||
                pro.credentials.any { it.lowercase().contains(q) }
        }
    }
}
