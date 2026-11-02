package com.campusconnect.app.core.data

import com.campusconnect.app.core.model.Accommodation
import com.campusconnect.app.core.model.CampusEvent
import com.campusconnect.app.core.model.CampusPointOfInterest
import com.campusconnect.app.core.model.CommunityGroup
import com.campusconnect.app.core.model.CommunityType
import com.campusconnect.app.core.model.Conversation
import com.campusconnect.app.core.model.Course
import com.campusconnect.app.core.model.Department
import com.campusconnect.app.core.model.Institution
import com.campusconnect.app.core.model.Listing
import com.campusconnect.app.core.model.ListingCategory
import com.campusconnect.app.core.model.ListingStatus
import com.campusconnect.app.core.model.Message
import com.campusconnect.app.core.model.NotificationItem
import com.campusconnect.app.core.model.NotificationType
import com.campusconnect.app.core.model.Opportunity
import com.campusconnect.app.core.model.OpportunityType
import com.campusconnect.app.core.model.StudentSummary

/**
 * Fictional, clearly-labelled sample content used when no backend is configured.
 * None of the people, employers, prices or campus details below are official.
 * Replace this data by connecting Supabase (see docs/DEPLOYMENT.md).
 */
object DemoData {

    const val DEMO_INSTITUTION_ID = "nnp"

    private const val HOUR = 3_600_000L
    private const val DAY = 86_400_000L

    fun now(): Long = System.currentTimeMillis()

    val institutions = listOf(
        Institution(
            id = DEMO_INSTITUTION_ID,
            name = "Nairobi National Polytechnic",
            shortName = "NNP",
            city = "Nairobi",
        ),
    )

    val departments = listOf(
        Department("dept-ict", DEMO_INSTITUTION_ID, "Computing & Informatics (sample)"),
        Department("dept-business", DEMO_INSTITUTION_ID, "Business & Management (sample)"),
        Department("dept-engineering", DEMO_INSTITUTION_ID, "Engineering & Technology (sample)"),
        Department("dept-science", DEMO_INSTITUTION_ID, "Applied Sciences (sample)"),
        Department("dept-hospitality", DEMO_INSTITUTION_ID, "Hospitality & Tourism (sample)"),
    )

    val courses = listOf(
        Course("course-ict", "dept-ict", "Diploma in Information Communication Technology (sample)"),
        Course("course-business", "dept-business", "Diploma in Business Management (sample)"),
        Course("course-electrical", "dept-engineering", "Diploma in Electrical Engineering (sample)"),
        Course("course-biology", "dept-science", "Diploma in Applied Biology (sample)"),
        Course("course-catering", "dept-hospitality", "Diploma in Catering & Accommodation (sample)"),
    )

    fun categories() = listOf(
        ListingCategory("cat-books", "Books & Notes"),
        ListingCategory("cat-electronics", "Electronics"),
        ListingCategory("cat-fashion", "Fashion"),
        ListingCategory("cat-food", "Food & Snacks"),
        ListingCategory("cat-services", "Services"),
        ListingCategory("cat-hostel", "Hostel Items"),
    )

    fun students(now: Long = now()) = listOf(
        StudentSummary("u-amina", "Amina Wanjiku", "Computing & Informatics (sample)",
            "Diploma in ICT (sample)", 2, "Frontend learner. Loves UI design and coffee."),
        StudentSummary("u-brian", "Brian Otieno", "Engineering & Technology (sample)",
            "Diploma in Electrical Engineering (sample)", 3, "Electronics tinkerer. Campus robotics club."),
        StudentSummary("u-grace", "Grace Mwangi", "Business & Management (sample)",
            "Diploma in Business Management (sample)", 1, "Aspiring entrepreneur. Selling snacks on campus."),
        StudentSummary("u-kevin", "Kevin Kiprop", "Applied Sciences (sample)",
            "Diploma in Applied Biology (sample)", 2, "Lab enthusiast and part-time tutor."),
        StudentSummary("u-fatuma", "Fatuma Ali", "Hospitality & Tourism (sample)",
            "Diploma in Catering (sample)", 3, "Baking, events and hospitality."),
        StudentSummary("u-john", "John Kamau", "Computing & Informatics (sample)",
            "Diploma in ICT (sample)", 4, "Backend developer. Looking for attachments."),
    )

    fun groups() = listOf(
        CommunityGroup("grp-ict", "ICT Students Hub", CommunityType.DEPARTMENT,
            "Share resources, revision notes and tech events. Sample community.", 214),
        CommunityGroup("grp-year1", "Year 1 Class Group", CommunityType.CLASS,
            "Class updates, timetables and group work. Sample community.", 86),
        CommunityGroup("grp-robotics", "Robotics & Innovation Club", CommunityType.CLUB,
            "Weekly build sessions and hackathons. Sample community.", 47),
        CommunityGroup("grp-entrepreneurs", "Campus Entrepreneurs", CommunityType.CLUB,
            "Ideas, side hustles and pitching practice. Sample community.", 132),
        CommunityGroup("grp-general", "Campus Noticeboard", CommunityType.GENERAL,
            "General campus conversation and announcements. Sample community.", 540),
    )

    fun posts(now: Long = now()) = listOf(
        com.campusconnect.app.core.model.Post(
            id = "post-1", authorId = "u-amina", authorName = "Amina Wanjiku",
            authorDepartment = "Computing & Informatics (sample)",
            content = "The library study rooms are easiest to get before 9am. Anyone up for a group revision session on databases this week?",
            createdAt = now - 2 * HOUR, likeCount = 34, commentCount = 3, likedByMe = false,
        ),
        com.campusconnect.app.core.model.Post(
            id = "post-2", authorId = "u-grace", authorName = "Grace Mwangi",
            authorDepartment = "Business & Management (sample)",
            content = "I am selling fresh mandazi and tea at the student centre every morning this week. DM me for bulk orders for your class!",
            createdAt = now - 5 * HOUR, likeCount = 21, commentCount = 1, likedByMe = false,
            communityName = "Campus Noticeboard",
        ),
        com.campusconnect.app.core.model.Post(
            id = "post-3", authorId = "u-brian", authorName = "Brian Otieno",
            authorDepartment = "Engineering & Technology (sample)",
            content = "Robotics club build session this Friday at the ICT lab. Beginners welcome, just bring your curiosity.",
            createdAt = now - 9 * HOUR, likeCount = 48, commentCount = 2, likedByMe = false,
            communityName = "Robotics & Innovation Club",
        ),
        com.campusconnect.app.core.model.Post(
            id = "post-4", authorId = "u-kevin", authorName = "Kevin Kiprop",
            authorDepartment = "Applied Sciences (sample)",
            content = "Sharing my revision notes for the applied biology practical. Reply here and I will send the PDF to the group.",
            createdAt = now - 1 * DAY, likeCount = 16, commentCount = 0, likedByMe = false,
        ),
    )

    fun comments(now: Long = now()) = mapOf(
        "post-1" to listOf(
            com.campusconnect.app.core.model.Comment("c1", "post-1", "u-kevin", "Kevin Kiprop",
                "Count me in. I can bring practice questions.", now - 1 * HOUR),
            com.campusconnect.app.core.model.Comment("c2", "post-1", "u-john", "John Kamau",
                "Room 4 is usually free early. Let us meet at 8:30am.", now - 40 * 60_000L),
            com.campusconnect.app.core.model.Comment("c3", "post-1", "u-amina", "Amina Wanjiku",
                "Perfect, I will create a group chat.", now - 20 * 60_000L),
        ),
        "post-2" to listOf(
            com.campusconnect.app.core.model.Comment("c4", "post-2", "u-fatuma", "Fatuma Ali",
                "I will order 20 for our class on Thursday.", now - 3 * HOUR),
        ),
        "post-3" to listOf(
            com.campusconnect.app.core.model.Comment("c5", "post-3", "u-john", "John Kamau",
                "I will bring the Arduino kit.", now - 6 * HOUR),
            com.campusconnect.app.core.model.Comment("c6", "post-3", "u-amina", "Amina Wanjiku",
                "Can design students join too?", now - 4 * HOUR),
        ),
    )

    fun listings(now: Long = now()) = listOf(
        Listing("lst-1", "Casio Scientific Calculator", "Graphing-ready scientific calculator in good condition. Used for one semester.",
            1800, "cat-electronics", "Electronics", "u-john", "John Kamau", 4.7,
            location = "Campus gate", createdAt = now - 3 * HOUR),
        Listing("lst-2", "ICT Revision Notes Bundle", "Printed summaries for databases, networking and programming fundamentals.",
            350, "cat-books", "Books & Notes", "u-amina", "Amina Wanjiku", 4.9,
            location = "Main library", createdAt = now - 8 * HOUR),
        Listing("lst-3", "Hostel Reading Lamp", "Adjustable LED desk lamp. Perfect for night study in the hostel.",
            900, "cat-hostel", "Hostel Items", "u-fatuma", "Fatuma Ali", 4.4,
            location = "Student village", createdAt = now - 1 * DAY),
        Listing("lst-4", "Laptop Backpack", "Water-resistant 15.6 inch laptop backpack with USB port.",
            1500, "cat-fashion", "Fashion", "u-brian", "Brian Otieno", 4.2,
            location = "Engineering block", createdAt = now - 2 * DAY),
        Listing("lst-5", "Campus Printing Service", "Printing, binding and laminating. Same-day pickup near the library.",
            20, "cat-services", "Services", "u-kevin", "Kevin Kiprop", 4.8,
            location = "Near library", createdAt = now - 3 * DAY),
        Listing("lst-6", "Home-baked Cookies (Pack)", "A dozen chocolate chip cookies, baked fresh each morning.",
            250, "cat-food", "Food & Snacks", "u-grace", "Grace Mwangi", 4.6,
            location = "Student centre", createdAt = now - 4 * DAY),
    )

    fun opportunities(now: Long = now()) = listOf(
        Opportunity("opp-1", "Junior Frontend Developer Attachment", "Sample Fintech Startup",
            OpportunityType.ATTACHMENT,
            "Work with a small product team on React and mobile screens. Mentorship provided. This is sample listing data.",
            "Nairobi", now + 12 * DAY, "KES 15,000 / month", now - 4 * HOUR,
            eligibility = "2nd/3rd year students in ICT or related fields"),
        Opportunity("opp-2", "Campus Brand Ambassador", "Nairobi Design Collective (sample)",
            OpportunityType.GIG,
            "Represent a student design collective at campus events. Flexible hours, stipend per activation. Sample listing.",
            "Nairobi", now + 6 * DAY, "KES 8,000 / month", now - 1 * DAY,
            remote = true),
        Opportunity("opp-3", "Data Entry Internship", "Sample Business Services Ltd",
            OpportunityType.INTERNSHIP,
            "Support a records digitisation project. Training on tools provided. Sample listing data.",
            "Nairobi", now + 20 * DAY, "KES 12,000 / month", now - 2 * DAY),
        Opportunity("opp-4", "Part-time Maths Tutor", "Campus Learning Hub (sample)",
            OpportunityType.GIG,
            "Tutor first-year students in mathematics. Two evenings a week. Sample listing data.",
            "Campus", now + 9 * DAY, "KES 500 / session", now - 3 * DAY),
        Opportunity("opp-5", "Graduate Trainee Programme", "Sample Engineering Group",
            OpportunityType.JOB,
            "Structured one-year graduate trainee programme for final-year students. Sample listing data.",
            "Nairobi", now + 30 * DAY, "Competitive", now - 5 * DAY),
        Opportunity("opp-6", "Industrial Attachment - Lab Assistant", "Sample Applied Sciences Institute",
            OpportunityType.ATTACHMENT,
            "Support lab preparation and sample handling for a semester. Sample listing data.",
            "Nairobi", now + 15 * DAY, "Transport allowance", now - 6 * DAY,
            eligibility = "Applied Sciences students, 3rd year"),
    )

    fun accommodation(now: Long = now()) = listOf(
        Accommodation("acc-1", "Single Room near Campus (sample)",
            "Clean single room with water and electricity included. Shared kitchen. Sample listing.",
            6500, "Hostel Zone A (sample)", "7 min walk", 1,
            landlordName = "Demo Landlord", landlordPhone = "+254700000001",
            available = true, verified = true),
        Accommodation("acc-2", "Shared Two-Bedroom Unit (sample)",
            "Two students sharing a two-bedroom unit. Furnished sitting room. Sample listing.",
            4500, "Hostel Zone B (sample)", "12 min walk", 2,
            landlordName = "Demo Landlord 2", landlordPhone = "+254700000002",
            available = true, verified = false),
        Accommodation("acc-3", "Hostel Bed - Female Wing (sample)",
            "Bed space in a managed female hostel with study room and security. Sample listing.",
            5500, "Hostel Zone C (sample)", "5 min walk", 1,
            landlordName = "Demo Hostel Office", landlordPhone = "+254700000003",
            available = false, verified = true),
        Accommodation("acc-4", "Bedsitter with Kitchenette (sample)",
            "Self-contained bedsitter, prepaid electricity, borehole water. Sample listing.",
            8000, "Hostel Zone D (sample)", "15 min walk", 1,
            landlordName = "Demo Landlord 3", landlordPhone = "+254700000004",
            available = true, verified = false),
    )

    fun events(now: Long = now()) = listOf(
        CampusEvent("evt-1", "Freshers Orientation (sample)",
            "Meet clubs, societies and support services. Sample event for demonstration.",
            now + 2 * DAY, "Main Hall (sample)", "Student Affairs (sample)", rsvpCount = 128),
        CampusEvent("evt-2", "Campus Career Fair (sample)",
            "Meet sample employers and learn about attachments and internships.",
            now + 6 * DAY, "Sports Ground (sample)", "Careers Office (sample)", rsvpCount = 76),
        CampusEvent("evt-3", "Robotics Club Hackathon (sample)",
            "A one-day build challenge for beginners and experienced makers.",
            now + 9 * DAY, "ICT Lab (sample)", "Robotics & Innovation Club", rsvpCount = 34),
        CampusEvent("evt-4", "Entrepreneurs Pitch Night (sample)",
            "Student founders pitch ideas to a friendly panel and peers.",
            now + 14 * DAY, "Student Centre (sample)", "Campus Entrepreneurs", rsvpCount = 52),
    )

    fun conversations(now: Long = now()) = listOf(
        Conversation("conv-1", "Amina Wanjiku", listOf("u-amina"),
            "See you at the library at 8:30am.", now - 25 * 60_000L, unread = 2, online = true),
        Conversation("conv-2", "ICT Study Group", listOf("u-amina", "u-john", "u-kevin"),
            "John: I uploaded the networking notes.", now - 2 * HOUR, unread = 0, isGroup = true),
        Conversation("conv-3", "Brian Otieno", listOf("u-brian"),
            "The Arduino kit is working now.", now - 1 * DAY, unread = 0),
        Conversation("conv-4", "Campus Entrepreneurs", listOf("u-grace", "u-fatuma", "u-john", "u-amina"),
            "Grace: Pitch night sign-up is open.", now - 2 * DAY, unread = 1, isGroup = true),
    )

    fun messages(now: Long = now()) = mapOf(
        "conv-1" to listOf(
            Message("msg-1", "conv-1", "u-amina", "Amina Wanjiku",
                "Hi! Are you joining the database revision session?", now - 45 * 60_000L),
            Message("msg-2", "conv-1", "u-me", "You",
                "Yes, I will be there. Which room?", now - 38 * 60_000L, isMine = true),
            Message("msg-3", "conv-1", "u-amina", "Amina Wanjiku",
                "Room 4 in the library. I will bring past papers.", now - 30 * 60_000L),
            Message("msg-4", "conv-1", "u-amina", "Amina Wanjiku",
                "See you at the library at 8:30am.", now - 25 * 60_000L),
        ),
        "conv-2" to listOf(
            Message("msg-5", "conv-2", "u-john", "John Kamau",
                "I uploaded the networking notes.", now - 2 * HOUR),
            Message("msg-6", "conv-2", "u-kevin", "Kevin Kiprop",
                "Thanks! The subnetting examples are useful.", now - 100 * 60_000L),
            Message("msg-7", "conv-2", "u-me", "You",
                "I will add my database summary tonight.", now - 90 * 60_000L, isMine = true),
        ),
        "conv-3" to listOf(
            Message("msg-8", "conv-3", "u-me", "You",
                "Did the replacement sensor arrive?", now - 26 * HOUR, isMine = true),
            Message("msg-9", "conv-3", "u-brian", "Brian Otieno",
                "The Arduino kit is working now.", now - 25 * HOUR),
        ),
        "conv-4" to listOf(
            Message("msg-10", "conv-4", "u-grace", "Grace Mwangi",
                "Pitch night sign-up is open.", now - 2 * DAY),
            Message("msg-11", "conv-4", "u-fatuma", "Fatuma Ali",
                "I will bring snacks for the team.", now - 47 * HOUR),
        ),
    )

    fun notifications(now: Long = now()) = listOf(
        NotificationItem("not-1", "Library hours extended",
            "The library is open until 10pm during the sample exam period.",
            NotificationType.ANNOUNCEMENT, now - 30 * 60_000L),
        NotificationItem("not-2", "New message from Amina",
            "See you at the library at 8:30am.", NotificationType.MESSAGE, now - 25 * 60_000L, targetId = "conv-1"),
        NotificationItem("not-3", "Career Fair next week",
            "RSVP for the sample campus career fair.", NotificationType.EVENT, now - 4 * HOUR, targetId = "evt-2"),
        NotificationItem("not-4", "New attachment opportunity",
            "Junior Frontend Developer attachment closes in 12 days.",
            NotificationType.OPPORTUNITY, now - 6 * HOUR, targetId = "opp-1"),
        NotificationItem("not-5", "Your listing is live",
            "Sample moderation approved your listing.", NotificationType.MARKETPLACE,
            now - 1 * DAY, read = true, targetId = "lst-2"),
    )

    fun pointsOfInterest() = listOf(
        CampusPointOfInterest("poi-1", "Main Library", "Study rooms, printing and book lending (sample POI).",
            "Study", -1.29210, 36.82190),
        CampusPointOfInterest("poi-2", "Lecture Hall A", "General lecture hall (sample POI).",
            "Lecture", -1.29250, 36.82230),
        CampusPointOfInterest("poi-3", "Administration Block", "Admissions and student records (sample POI).",
            "Office", -1.29170, 36.82160),
        CampusPointOfInterest("poi-4", "Student Centre", "Food, clubs and student services (sample POI).",
            "Services", -1.29280, 36.82150),
        CampusPointOfInterest("poi-5", "ICT Lab", "Computer labs and club sessions (sample POI).",
            "Lab", -1.29230, 36.82270),
        CampusPointOfInterest("poi-6", "Sports Ground", "Sports and outdoor events (sample POI).",
            "Sports", -1.29320, 36.82200),
        CampusPointOfInterest("poi-7", "Cafeteria", "Student cafeteria (sample POI).",
            "Food", -1.29260, 36.82120),
        CampusPointOfInterest("poi-8", "Health Centre", "First aid and campus clinic (sample POI).",
            "Health", -1.29190, 36.82240),
    )
}
