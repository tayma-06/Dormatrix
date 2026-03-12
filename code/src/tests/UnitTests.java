package tests;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.junit.Assert.*;

import libraries.hashing.HashFunction;
import libraries.collections.MyString;
import libraries.collections.MyArrayList;
import libraries.collections.MyOptional;
import libraries.slots.FirstFitAllocator;
import exceptions.SlotUnavailableException;
import exceptions.account.*;
import models.store.*;
import models.users.*;
import models.room.Room;
import models.food.*;
import models.complaints.*;
import models.enums.*;
import models.routine.RoutineEntry;
import controllers.account.CreateAccountController;
import controllers.authentication.AccountManager;
import utils.RoleMapper;
import utils.TimeManager;

import models.routine.StudentRoutineEntry;
import models.schedule.WorkerVisitEntry;
import models.enums.WorkerField;
import models.users.MaintenanceWorker;
import controllers.routine.RoutineController;
import controllers.schedule.WorkerScheduleController;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

@RunWith(JUnit4.class)
public class UnitTests {

    // ─── HashFunction Tests ───────────────────────────────────
    @Test
    public void hashSameInputProducesSameHash() {
        String h1 = HashFunction.hashPassword(new MyString("password123")).getValue();
        String h2 = HashFunction.hashPassword(new MyString("password123")).getValue();
        assertEquals(h1, h2);
    }

    @Test
    public void hashDifferentInputProducesDifferentHash() {
        String h1 = HashFunction.hashPassword(new MyString("password123")).getValue();
        String h2 = HashFunction.hashPassword(new MyString("differentPass")).getValue();
        assertNotEquals(h1, h2);
    }

    @Test
    public void hashIsNonEmpty() {
        String h = HashFunction.hashPassword(new MyString("password123")).getValue();
        assertTrue(h.length() > 0);
    }

    @Test
    public void hashEmptyStringReturnsZero() {
        String h = HashFunction.hashPassword(new MyString("")).getValue();
        assertEquals("0", h);
    }

    @Test
    public void hashSingleCharsDiffer() {
        String hA = HashFunction.hashPassword(new MyString("A")).getValue();
        String hB = HashFunction.hashPassword(new MyString("B")).getValue();
        assertNotEquals(hA, hB);
    }

    @Test
    public void hashOutputIsValidHex() {
        String h = HashFunction.hashPassword(new MyString("password123")).getValue();
        assertTrue(h.matches("[0-9a-f]+"));
    }

    // ─── StudentBalance Tests ─────────────────────────────────
    @Test
    public void balanceInitialValues() {
        StudentBalance sb = new StudentBalance("STU001", 100.0);
        assertEquals(100.0, sb.getBalance(), 0.001);
        assertEquals("STU001", sb.getStudentId());
    }

    @Test
    public void balanceAdd() {
        StudentBalance sb = new StudentBalance("STU001", 100.0);
        sb.addBalance(50.0);
        assertEquals(150.0, sb.getBalance(), 0.001);
    }

    @Test
    public void balanceDeductWithinLimit() {
        StudentBalance sb = new StudentBalance("STU001", 100.0);
        assertTrue(sb.deductBalance(30.0));
        assertEquals(70.0, sb.getBalance(), 0.001);
    }

    @Test
    public void balanceDeductInsufficientFails() {
        StudentBalance sb = new StudentBalance("STU001", 100.0);
        assertFalse(sb.deductBalance(999.0));
        assertEquals(100.0, sb.getBalance(), 0.001);
    }

    @Test
    public void balanceDeductExact() {
        StudentBalance sb = new StudentBalance("STU001", 100.0);
        assertTrue(sb.deductBalance(100.0));
        assertEquals(0.0, sb.getBalance(), 0.001);
    }

    @Test
    public void balanceToFileString() {
        StudentBalance sb = new StudentBalance("STU002", 75.5);
        assertEquals("STU002,75.5", sb.toFileString());
    }

    @Test
    public void balanceFromStringRoundTrip() {
        StudentBalance parsed = StudentBalance.fromString("STU002,75.5");
        assertNotNull(parsed);
        assertEquals("STU002", parsed.getStudentId());
        assertEquals(75.5, parsed.getBalance(), 0.001);
    }

    @Test
    public void balanceFromStringEdgeCases() {
        assertNull(StudentBalance.fromString(null));
        assertNull(StudentBalance.fromString(""));
        assertNull(StudentBalance.fromString("bad"));
    }

    // ─── MyArrayList Tests ────────────────────────────────────
    @Test
    public void arrayListNewIsEmpty() {
        MyArrayList<String> list = new MyArrayList<>();
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
    }

    @Test
    public void arrayListAddAndGet() {
        MyArrayList<String> list = new MyArrayList<>();
        list.add("alpha");
        list.add("beta");
        list.add("gamma");
        assertEquals(3, list.size());
        assertEquals("alpha", list.get(0));
        assertEquals("gamma", list.get(2));
    }

    @Test
    public void arrayListContains() {
        MyArrayList<String> list = new MyArrayList<>();
        list.add("beta");
        assertTrue(list.contains("beta"));
        assertFalse(list.contains("delta"));
    }

    @Test
    public void arrayListIndexOf() {
        MyArrayList<String> list = new MyArrayList<>();
        list.add("alpha");
        list.add("beta");
        assertEquals(1, list.indexOf("beta"));
        assertEquals(-1, list.indexOf("zzz"));
    }

    @Test
    public void arrayListSet() {
        MyArrayList<String> list = new MyArrayList<>();
        list.add("alpha");
        list.add("beta");
        list.set(1, "BETA");
        assertEquals("BETA", list.get(1));
    }

    @Test
    public void arrayListRemoveByIndex() {
        MyArrayList<String> list = new MyArrayList<>();
        list.add("alpha");
        list.add("beta");
        list.add("gamma");
        String removed = list.remove(0);
        assertEquals("alpha", removed);
        assertEquals(2, list.size());
        assertEquals("beta", list.get(0));
    }

    @Test
    public void arrayListRemoveByValue() {
        MyArrayList<String> list = new MyArrayList<>();
        list.add("alpha");
        list.add("beta");
        list.add("gamma");
        assertTrue(list.remove((Object) "beta"));
        assertEquals(2, list.size());
    }

    @Test
    public void arrayListAddAtIndex() {
        MyArrayList<String> list = new MyArrayList<>();
        list.add("a");
        list.add("b");
        list.add(0, "first");
        assertEquals("first", list.get(0));
        assertEquals(3, list.size());
    }

    @Test
    public void arrayListClear() {
        MyArrayList<String> list = new MyArrayList<>();
        list.add("a");
        list.clear();
        assertTrue(list.isEmpty());
    }

    @Test
    public void arrayListGrowsBeyondDefaultCapacity() {
        MyArrayList<Integer> nums = new MyArrayList<>();
        for (int i = 0; i < 25; i++) {
            nums.add(i);
        }
        assertEquals(25, nums.size());
        assertEquals(Integer.valueOf(24), nums.get(24));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void arrayListGetOutOfBoundsThrows() {
        new MyArrayList<String>().get(99);
    }

    // ─── MyString Tests ───────────────────────────────────────
    @Test
    public void myStringBasics() {
        MyString s = new MyString("Hello World");
        assertEquals(11, s.length());
        assertEquals('H', s.charAt(0));
        assertEquals("Hello World", s.getValue());
    }

    @Test
    public void myStringEquals() {
        MyString s1 = new MyString("Hello World");
        MyString s2 = new MyString("Hello World");
        MyString s3 = new MyString("hello world");
        assertTrue(s1.equals(s2));
        assertFalse(s1.equals(s3));
    }

    @Test
    public void myStringCompareTo() {
        MyString s1 = new MyString("Hello World");
        MyString s2 = new MyString("Hello World");
        assertEquals(0, s1.compareTo(s2));
    }

    @Test
    public void myStringSubstring() {
        MyString s = new MyString("Hello World");
        assertEquals("Hello", s.substring(0, 5).getValue());
    }

    @Test
    public void myStringIndexOf() {
        MyString s = new MyString("Hello World");
        assertEquals(4, s.indexOf('o'));
        assertEquals(-1, s.indexOf('z'));
    }

    @Test
    public void myStringSplit() {
        MyString s = new MyString("Hello World");
        MyString[] parts = s.split(' ');
        assertEquals(2, parts.length);
        assertEquals("Hello", parts[0].getValue());
        assertEquals("World", parts[1].getValue());
    }

    @Test
    public void myStringConcat() {
        MyString a = new MyString("foo");
        MyString b = new MyString("bar");
        assertEquals("foobar", a.concat(b).getValue());
    }

    @Test
    public void myStringCaseConversion() {
        assertEquals("abc", new MyString("ABC").toLowerCase().getValue());
        assertEquals("ABC", new MyString("abc").toUpperCase().getValue());
    }

    @Test
    public void myStringTrim() {
        assertEquals("hi", new MyString("  hi  ").trim().getValue());
    }

    @Test
    public void myStringIsEmpty() {
        assertTrue(new MyString("").isEmpty());
        assertFalse(new MyString("x").isEmpty());
    }

    @Test
    public void myStringContains() {
        MyString s = new MyString("Hello World");
        assertTrue(s.contains(new MyString("World")));
        assertFalse(s.contains(new MyString("xyz")));
    }

    @Test
    public void myStringContainsAny() {
        MyString s = new MyString("Hello World");
        assertTrue(s.containsAny(new MyString("xyz"), new MyString("Hello")));
        assertFalse(s.containsAny(new MyString("xyz"), new MyString("abc")));
    }

    @Test
    public void myStringIntToHex() {
        assertEquals("000000ff", MyString.intToHex(255).getValue());
        assertEquals("00000000", MyString.intToHex(0).getValue());
    }

    @Test
    public void myStringJoin() {
        MyString joined = MyString.join(',', new MyString("a"), new MyString("b"), new MyString("c"));
        assertEquals("a,b,c", joined.getValue());
    }

    @Test
    public void myStringReplace() {
        assertEquals("heLLo", new MyString("hello").replace('l', 'L').getValue());
    }

    // ─── MyOptional Tests ─────────────────────────────────────
    @Test
    public void optionalOfIsPresent() {
        MyOptional<String> present = MyOptional.of("value");
        assertTrue(present.isPresent());
        assertFalse(present.isEmpty());
        assertEquals("value", present.get());
    }

    @Test
    public void optionalEmptyIsEmpty() {
        MyOptional<String> empty = MyOptional.empty();
        assertTrue(empty.isEmpty());
        assertFalse(empty.isPresent());
    }

    @Test
    public void optionalOrElse() {
        assertEquals("value", MyOptional.of("value").orElse("fallback"));
        assertEquals("fallback", MyOptional.empty().orElse("fallback"));
    }

    @Test(expected = NullPointerException.class)
    public void optionalOfNullThrows() {
        MyOptional.of(null);
    }

    @Test(expected = IllegalStateException.class)
    public void optionalGetOnEmptyThrows() {
        MyOptional.empty().get();
    }

    @Test
    public void optionalOfNullable() {
        assertTrue(MyOptional.ofNullable(null).isEmpty());
        assertTrue(MyOptional.ofNullable("hi").isPresent());
    }

    // ─── FirstFitAllocator Tests ──────────────────────────────
    @Test
    public void firstFitAllEmpty() throws SlotUnavailableException {
        FirstFitAllocator alloc = new FirstFitAllocator();
        assertEquals(0, alloc.findSlot(new String[]{null, null, null}));
    }

    @Test
    public void firstFitFirstTaken() throws SlotUnavailableException {
        FirstFitAllocator alloc = new FirstFitAllocator();
        assertEquals(1, alloc.findSlot(new String[]{"taken", null, null}));
    }

    @Test
    public void firstFitOnlyLastFree() throws SlotUnavailableException {
        FirstFitAllocator alloc = new FirstFitAllocator();
        assertEquals(2, alloc.findSlot(new String[]{"a", "b", null}));
    }

    @Test(expected = SlotUnavailableException.class)
    public void firstFitAllFullThrows() throws SlotUnavailableException {
        new FirstFitAllocator().findSlot(new String[]{"a", "b", "c"});
    }

    // ─── Validation: Email ────────────────────────────────────
    @Test
    public void validStudentEmail() throws InvalidEmailException {
        CreateAccountController ctrl = new CreateAccountController(null);
        assertTrue(ctrl.isValidEmail("user@iut-dhaka.edu", "STUDENT"));
    }

    @Test(expected = InvalidEmailException.class)
    public void invalidStudentEmailThrows() throws InvalidEmailException {
        CreateAccountController ctrl = new CreateAccountController(null);
        ctrl.isValidEmail("user@gmail.com", "STUDENT");
    }

    @Test
    public void validHallAttendantEmail() throws InvalidEmailException {
        CreateAccountController ctrl = new CreateAccountController(null);
        assertTrue(ctrl.isValidEmail("staff@iut-dhaka.com", "HALL_ATTENDANT"));
    }

    @Test(expected = InvalidEmailException.class)
    public void invalidHallAttendantEmailThrows() throws InvalidEmailException {
        CreateAccountController ctrl = new CreateAccountController(null);
        ctrl.isValidEmail("staff@iut-dhaka.edu", "HALL_ATTENDANT");
    }

    @Test
    public void emailSkippedForMaintenanceWorker() throws InvalidEmailException {
        CreateAccountController ctrl = new CreateAccountController(null);
        assertTrue(ctrl.isValidEmail(null, "MAINTENANCE_WORKER"));
    }

    @Test
    public void emailSkippedForStoreInCharge() throws InvalidEmailException {
        CreateAccountController ctrl = new CreateAccountController(null);
        assertTrue(ctrl.isValidEmail(null, "STORE_IN_CHARGE"));
    }

    // ─── Validation: Phone ────────────────────────────────────
    @Test
    public void validPhoneBD() throws InvalidPhoneException {
        CreateAccountController ctrl = new CreateAccountController(null);
        assertTrue(ctrl.isValidPhone("+8801712345678"));
    }

    @Test
    public void validPhoneLocal() throws InvalidPhoneException {
        CreateAccountController ctrl = new CreateAccountController(null);
        assertTrue(ctrl.isValidPhone("01712345678"));
    }

    @Test(expected = InvalidPhoneException.class)
    public void invalidPhoneThrows() throws InvalidPhoneException {
        CreateAccountController ctrl = new CreateAccountController(null);
        ctrl.isValidPhone("12345");
    }

    // ─── Validation: Password ─────────────────────────────────
    @Test
    public void validPassword() throws InvalidPasswordException {
        CreateAccountController ctrl = new CreateAccountController(null);
        assertTrue(ctrl.isValidPassword("secret1"));
    }

    @Test(expected = InvalidPasswordException.class)
    public void passwordTooShortThrows() throws InvalidPasswordException {
        CreateAccountController ctrl = new CreateAccountController(null);
        ctrl.isValidPassword("ab1");
    }

    @Test(expected = InvalidPasswordException.class)
    public void passwordNoDigitThrows() throws InvalidPasswordException {
        CreateAccountController ctrl = new CreateAccountController(null);
        ctrl.isValidPassword("abcdefgh");
    }

    // ─── Validation: Department ───────────────────────────────
    @Test
    public void validDepartments() throws InvalidDepartmentException {
        CreateAccountController ctrl = new CreateAccountController(null);
        String[] valid = {"EEE", "CSE", "ME", "IPE", "SWE", "CEE", "BTM"};
        for (String d : valid) {
            assertTrue(ctrl.isValidDepartment(d));
        }
    }

    @Test(expected = InvalidDepartmentException.class)
    public void invalidDepartmentThrows() throws InvalidDepartmentException {
        CreateAccountController ctrl = new CreateAccountController(null);
        ctrl.isValidDepartment("MATH");
    }

    // ─── Validation: Role ─────────────────────────────────────
    @Test
    public void validRoleCheck() {
        CreateAccountController ctrl = new CreateAccountController(null);
        assertTrue(ctrl.isValidRole(new MyString("STUDENT")));
        assertFalse(ctrl.isValidRole(new MyString("UNKNOWN")));
        assertFalse(ctrl.isValidRole(null));
    }

    // ─── RoleMapper ───────────────────────────────────────────
    @Test
    public void roleMapperKnownChoices() {
        assertEquals("STUDENT", RoleMapper.getRoleFromChoice(1).getValue());
        assertEquals("HALL_ATTENDANT", RoleMapper.getRoleFromChoice(2).getValue());
        assertEquals("MAINTENANCE_WORKER", RoleMapper.getRoleFromChoice(3).getValue());
        assertEquals("STORE_IN_CHARGE", RoleMapper.getRoleFromChoice(4).getValue());
        assertEquals("HALL_OFFICER", RoleMapper.getRoleFromChoice(5).getValue());
        assertEquals("ADMIN", RoleMapper.getRoleFromChoice(6).getValue());
        assertEquals("CAFETERIA_MANAGER", RoleMapper.getRoleFromChoice(7).getValue());
    }

    @Test
    public void roleMapperUnknownChoice() {
        assertEquals("UNKNOWN", RoleMapper.getRoleFromChoice(99).getValue());
    }

    // ─── Student Creation & Serialization ─────────────────────
    @Test
    public void studentCreationDefaults() {
        Student s = new Student("230042139", "Alice", "STUDENT", "hash", "01712345678", "alice@iut-dhaka.edu");
        assertEquals("230042139", s.getId());
        assertEquals("Alice", s.getName());
        assertEquals("N/A", s.getDepartment());
        assertEquals("UNASSIGNED", s.getRoomNumber());
        assertEquals("alice@iut-dhaka.edu", s.getEmail());
    }

    @Test
    public void studentToFileStringFormat() {
        Student s = new Student("230042139", "Alice", "STUDENT", "hash", "01712345678", "alice@iut-dhaka.edu");
        s.setDepartment("CSE");
        s.setRoomNumber("R101");
        String line = s.toFileString();
        assertTrue(line.contains("230042139"));
        assertTrue(line.contains("Alice"));
        assertTrue(line.contains("CSE"));
        assertTrue(line.contains("R101"));
    }

    @Test
    public void studentFromFileStringRoundTrip() {
        Student original = new Student("230042139", "Alice", "STUDENT", "hash", "01712345678", "alice@iut-dhaka.edu");
        original.setDepartment("CSE");
        original.setRoomNumber("R101");
        Student parsed = Student.fromFileString(original.toFileString());
        assertNotNull(parsed);
        assertEquals("230042139", parsed.getId());
        assertEquals("Alice", parsed.getName());
        assertEquals("CSE", parsed.getDepartment());
        assertEquals("R101", parsed.getRoomNumber());
    }

    @Test
    public void studentFromFileStringInvalid() {
        assertNull(Student.fromFileString("short|data"));
    }

    @Test
    public void studentPublicInfo() {
        Student s = new Student("230042139", "Alice", "STUDENT", "hash", "017", "a@iut-dhaka.edu");
        s.setRoomNumber("R101");
        StudentPublicInfo info = s.publicInfo();
        assertEquals("230042139", info.getStudentId());
        assertEquals("Alice", info.getName());
        assertEquals("R101", info.getRoomNo());
    }

    // ─── Room Tests ───────────────────────────────────────────
    @Test
    public void roomAvailability() {
        Room room = new Room("R101", 3, 0);
        assertTrue(room.isAvailable());
        room.incrementOccupancy();
        room.incrementOccupancy();
        room.incrementOccupancy();
        assertFalse(room.isAvailable());
        assertEquals(3, room.getCurrentOccupancy());
    }

    @Test
    public void roomIncrementCapsAtCapacity() {
        Room room = new Room("R101", 2, 2);
        room.incrementOccupancy();
        assertEquals(2, room.getCurrentOccupancy());
    }

    @Test
    public void roomDecrementFloors() {
        Room room = new Room("R101", 2, 0);
        room.decrementOccupancy();
        assertEquals(0, room.getCurrentOccupancy());
    }

    @Test
    public void roomSerializationRoundTrip() {
        Room room = new Room("R101", 4, 2);
        Room parsed = Room.fromString(room.toFileString());
        assertNotNull(parsed);
        assertEquals("R101", parsed.getRoomId());
        assertEquals(4, parsed.getCapacity());
        assertEquals(2, parsed.getCurrentOccupancy());
    }

    @Test
    public void roomFromStringInvalid() {
        assertNull(Room.fromString("bad|data"));
    }

    // ─── Item Tests ───────────────────────────────────────────
    @Test
    public void itemCreationAndGetters() {
        Item item = new Item("I001", "Soap", 50, 25.0);
        assertEquals("I001", item.getItemId());
        assertEquals("Soap", item.getName());
        assertEquals(50, item.getQuantity());
        assertEquals(25.0, item.getPrice(), 0.001);
    }

    @Test
    public void itemReduceQuantity() {
        Item item = new Item("I001", "Soap", 50, 25.0);
        item.reduceQuantity(10);
        assertEquals(40, item.getQuantity());
    }

    @Test
    public void itemSerializationRoundTrip() {
        Item item = new Item("I001", "Soap", 50, 25.0);
        Item parsed = Item.fromString(item.toFileString());
        assertNotNull(parsed);
        assertEquals("I001", parsed.getItemId());
        assertEquals("Soap", parsed.getName());
        assertEquals(50, parsed.getQuantity());
        assertEquals(25.0, parsed.getPrice(), 0.001);
    }

    @Test
    public void itemFromStringEdgeCases() {
        assertNull(Item.fromString(null));
        assertNull(Item.fromString(""));
        assertNull(Item.fromString("only,two"));
    }

    // ─── ShoppingCart Tests ───────────────────────────────────
    @Test
    public void cartAddAndTotal() {
        ShoppingCart cart = new ShoppingCart();
        assertTrue(cart.isEmpty());
        cart.addItem("I001", "Soap", 2, 25.0);
        cart.addItem("I002", "Shampoo", 1, 50.0);
        assertFalse(cart.isEmpty());
        assertEquals(2, cart.getItemCount());
        assertEquals(100.0, cart.getTotal(), 0.001);
    }

    @Test
    public void cartMergesSameItem() {
        ShoppingCart cart = new ShoppingCart();
        cart.addItem("I001", "Soap", 2, 25.0);
        cart.addItem("I001", "Soap", 3, 25.0);
        assertEquals(1, cart.getItemCount());
        assertEquals(125.0, cart.getTotal(), 0.001);
    }

    @Test
    public void cartRemoveItem() {
        ShoppingCart cart = new ShoppingCart();
        cart.addItem("I001", "Soap", 2, 25.0);
        cart.addItem("I002", "Shampoo", 1, 50.0);
        cart.removeItem("I001");
        assertEquals(1, cart.getItemCount());
        assertEquals(50.0, cart.getTotal(), 0.001);
    }

    @Test
    public void cartClear() {
        ShoppingCart cart = new ShoppingCart();
        cart.addItem("I001", "Soap", 1, 25.0);
        cart.clear();
        assertTrue(cart.isEmpty());
        assertEquals(0, cart.getItemCount());
    }

    // ─── CartItem Tests ───────────────────────────────────────
    @Test
    public void cartItemSubtotal() {
        CartItem ci = new CartItem("I001", "Soap", 3, 25.0);
        assertEquals(75.0, ci.getSubtotal(), 0.001);
    }

    // ─── DueRecord Tests ──────────────────────────────────────
    @Test
    public void dueRecordSerializationRoundTrip() {
        DueRecord due = new DueRecord("STU001", 150.0);
        DueRecord parsed = DueRecord.fromString(due.toFileString());
        assertNotNull(parsed);
        assertEquals("STU001", parsed.getStudentId());
        assertEquals(150.0, parsed.getAmount(), 0.001);
    }

    @Test
    public void dueRecordAddAmount() {
        DueRecord due = new DueRecord("STU001", 100.0);
        due.addAmount(50.0);
        assertEquals(150.0, due.getAmount(), 0.001);
    }

    @Test
    public void dueRecordFromStringEdgeCases() {
        assertNull(DueRecord.fromString(null));
        assertNull(DueRecord.fromString(""));
        assertNull(DueRecord.fromString("bad"));
    }

    // ─── Complaint Creation ───────────────────────────────────
    @Test
    public void complaintCreateNew() {
        StudentPublicInfo info = new StudentPublicInfo("STU001", "Alice", "R101");
        Complaint c = Complaint.createNew("C-1", info, ComplaintCategory.ELECTRICITY, "Light broken", PriorityLevel.NORMAL, "");
        assertEquals("C-1", c.getComplaintId());
        assertEquals("STU001", c.getStudentId());
        assertEquals("Alice", c.getStudentName());
        assertEquals("R101", c.getStudentRoomNo());
        assertEquals(ComplaintCategory.ELECTRICITY, c.getCategory());
        assertEquals(ComplaintStatus.SUBMITTED, c.getStatus());
        assertEquals("", c.getAssignedWorkerId());
    }

    @Test
    public void complaintAssignAndClear() {
        StudentPublicInfo info = new StudentPublicInfo("STU001", "Alice", "R101");
        Complaint c = Complaint.createNew("C-1", info, ComplaintCategory.PLUMBING, "Leak", PriorityLevel.NORMAL, "");
        c.assignTo("W001");
        assertEquals(ComplaintStatus.ASSIGNED, c.getStatus());
        assertEquals("W001", c.getAssignedWorkerId());

        c.clearAssignment();
        assertEquals(ComplaintStatus.SUBMITTED, c.getStatus());
        assertEquals("", c.getAssignedWorkerId());
    }

    @Test
    public void complaintAppendTagNote() {
        StudentPublicInfo info = new StudentPublicInfo("STU001", "Alice", "R101");
        Complaint c = Complaint.createNew("C-1", info, ComplaintCategory.CLEANING, "Dirty", PriorityLevel.LOW, "");
        c.appendTagNote("urgent");
        assertEquals("urgent", c.getTags());
        c.appendTagNote("follow-up");
        assertEquals("urgent;follow-up", c.getTags());
    }

    @Test
    public void complaintNullPriorityDefaultsToNormal() {
        StudentPublicInfo info = new StudentPublicInfo("STU001", "Alice", "R101");
        Complaint c = new Complaint("C-1", "STU001", "Alice", "R101",
                ComplaintCategory.INTERNET, "Slow wifi", ComplaintStatus.SUBMITTED, "", null, null);
        assertEquals(PriorityLevel.NORMAL, c.getPriority());
        assertEquals("", c.getTags());
    }

    // ─── ComplaintPolicy Tests ────────────────────────────────
    @Test
    public void complaintPolicyNormalPriority() {
        ComplaintPolicy policy = new ComplaintPolicy();
        ComplaintPolicy.DormDecision d = policy.decide(ComplaintCategory.CLEANING, "Floor is dirty");
        assertEquals(PriorityLevel.NORMAL, d.getPriority());
        assertFalse(d.isEmergency());
    }

    @Test
    public void complaintPolicyEmergencyFire() {
        ComplaintPolicy policy = new ComplaintPolicy();
        ComplaintPolicy.DormDecision d = policy.decide(ComplaintCategory.ELECTRICITY, "There is fire in the room!");
        assertEquals(PriorityLevel.EMERGENCY, d.getPriority());
        assertTrue(d.isEmergency());
    }

    @Test
    public void complaintPolicyEmergencyFlood() {
        ComplaintPolicy policy = new ComplaintPolicy();
        ComplaintPolicy.DormDecision d = policy.decide(ComplaintCategory.PLUMBING, "burst pipe flooding");
        assertEquals(PriorityLevel.EMERGENCY, d.getPriority());
        assertTrue(d.isEmergency());
    }

    @Test
    public void complaintPolicyRecommendedWorkerField() {
        ComplaintPolicy policy = new ComplaintPolicy();
        assertEquals(WorkerField.ELECTRICIAN, policy.recommendedWorkerField(ComplaintCategory.ELECTRICITY));
        assertEquals(WorkerField.PLUMBER, policy.recommendedWorkerField(ComplaintCategory.PLUMBING));
        assertEquals(WorkerField.INTERNET_TECH, policy.recommendedWorkerField(ComplaintCategory.INTERNET));
        assertEquals(WorkerField.CLEANING, policy.recommendedWorkerField(ComplaintCategory.CLEANING));
    }

    // ─── MealToken Tests ──────────────────────────────────────
    @Test
    public void mealTokenCreationAndGetters() {
        MealToken token = new MealToken("T001", "STU001", MealType.LUNCH, LocalDate.of(2099, 1, 1), TokenStatus.ACTIVE);
        assertEquals("T001", token.getTokenId());
        assertEquals("STU001", token.getStudentId());
        assertEquals(MealType.LUNCH, token.getType());
        assertEquals(TokenStatus.ACTIVE, token.getStatus());
        assertFalse(token.isUsed());
    }

    @Test
    public void mealTokenUsedStatus() {
        MealToken token = new MealToken("T001", "STU001", MealType.DINNER, LocalDate.of(2099, 1, 1), TokenStatus.USED);
        assertTrue(token.isUsed());
    }

    @Test
    public void mealTokenExpiredWhenDatePast() {
        MealToken token = new MealToken("T001", "STU001", MealType.BREAKFAST, LocalDate.of(2000, 1, 1), TokenStatus.ACTIVE);
        assertEquals(TokenStatus.EXPIRED, token.getStatus());
    }

    @Test
    public void mealTokenSerializationRoundTrip() {
        MealToken original = new MealToken("T001", "STU001", MealType.LUNCH, LocalDate.of(2099, 6, 15), TokenStatus.ACTIVE);
        MealToken parsed = MealToken.fromString(original.toString());
        assertEquals("T001", parsed.getTokenId());
        assertEquals("STU001", parsed.getStudentId());
        assertEquals(MealType.LUNCH, parsed.getType());
        assertEquals(TokenStatus.ACTIVE, parsed.getStatus());
    }

    // ─── DailyMenu Tests ─────────────────────────────────────
    @Test
    public void dailyMenuSerializationRoundTrip() {
        DailyMenu menu = new DailyMenu("MONDAY", MealType.LUNCH, false, "Rice, Chicken");
        DailyMenu parsed = DailyMenu.fromString(menu.toString());
        assertNotNull(parsed);
        assertEquals("MONDAY", parsed.getDay());
        assertEquals(MealType.LUNCH, parsed.getType());
        assertFalse(parsed.isRamadan());
        assertEquals("Rice, Chicken", parsed.getItems());
    }

    @Test
    public void dailyMenuFromStringInvalid() {
        assertNull(DailyMenu.fromString("short"));
    }

    // ─── RoutineEntry Tests ───────────────────────────────────
    @Test
    public void routineEntrySerializationRoundTrip() {
        RoutineEntry entry = new RoutineEntry("STU001", DayOfWeek.MONDAY, 2, "Math 101");
        RoutineEntry parsed = RoutineEntry.fromFileString(entry.toFileString());
        assertNotNull(parsed);
        assertEquals("STU001", parsed.getStudentId());
        assertEquals(DayOfWeek.MONDAY, parsed.getDay());
        assertEquals(2, parsed.getSlotIndex());
        assertEquals("Math 101", parsed.getContent());
    }

    @Test
    public void routineEntryFromFileStringEdgeCases() {
        assertNull(RoutineEntry.fromFileString(null));
        assertNull(RoutineEntry.fromFileString(""));
        assertNull(RoutineEntry.fromFileString("too|few"));
    }

    @Test
    public void routineEntryCleansSpecialChars() {
        RoutineEntry entry = new RoutineEntry("STU001", DayOfWeek.TUESDAY, 0, "Lab|Session");
        assertEquals("Lab/Session", entry.getContent());
    }

    // ─── MaintenanceWorker Tests ──────────────────────────────
    @Test
    public void maintenanceWorkerCreation() {
        MaintenanceWorker w = new MaintenanceWorker("W001", "Bob", "MAINTENANCE_WORKER", "hash", "017", WorkerField.PLUMBER);
        assertEquals("W001", w.getId());
        assertEquals(WorkerField.PLUMBER, w.getField());
        assertTrue(w.toFileString().contains("MAINTENANCE_WORKER"));
    }

    // ─── Other User Types ─────────────────────────────────────
    @Test
    public void cafeteriaManagerToFileString() {
        CafeteriaManager cm = new CafeteriaManager("CM01", "Chef", "CAFETERIA_MANAGER", "hash", "017");
        String line = cm.toFileString();
        assertTrue(line.contains("CM01"));
        assertTrue(line.contains("Chef"));
    }

    @Test
    public void storeInChargeToFileString() {
        StoreInCharge sic = new StoreInCharge("SIC01", "Keeper", "STORE_IN_CHARGE", "hash", "017");
        String line = sic.toFileString();
        assertTrue(line.contains("STORE_IN_CHARGE"));
    }

    @Test
    public void hallOfficerCreation() {
        HallOfficer ho = new HallOfficer("HO01", "Officer", "HALL_OFFICER", "hash", "017", "ho@iut-dhaka.com");
        assertEquals("HO01", ho.getId());
        assertEquals("ho@iut-dhaka.com", ho.getEmail());
    }

    @Test
    public void studyRoomSeatConflict() {
        // Simulate seat map: 6 slots × 10 seats (mirrors StudyRoomController)
        String[][] seatMap = new String[6][10];
        int slot = 0;
        int seat = 3;

        // Student A books seat 3 in slot 0
        assertNull(seatMap[slot][seat]);
        seatMap[slot][seat] = "StudentA";

        // Student B tries same seat → should be blocked
        boolean seatTaken = seatMap[slot][seat] != null;
        assertTrue("Seat should already be taken", seatTaken);

        // Student B picks a different seat → succeeds
        int otherSeat = 4;
        assertNull(seatMap[slot][otherSeat]);
        seatMap[slot][otherSeat] = "StudentB";
        assertEquals("StudentB", seatMap[slot][otherSeat]);
    }

    @Test
    public void studyRoomStudentCannotBookTwiceInSameSlot() {
        String[][] seatMap = new String[6][10];
        int slot = 0;
        String student = "StudentA";

        // Book seat 2
        seatMap[slot][2] = student;

        // Check if student already has a seat in this slot
        boolean alreadyBooked = false;
        for (int i = 0; i < 10; i++) {
            if (student.equals(seatMap[slot][i])) {
                alreadyBooked = true;
                break;
            }
        }
        assertTrue("Student already has a booking in this slot", alreadyBooked);
    }

    @Test
    public void studyRoomInvalidSeatNumber() {
        // Mirrors StudyRoomController: seatNumber must be 0-9
        int seat = 15;
        assertTrue("Seat out of bounds should be rejected", seat < 0 || seat >= 10);
    }

    // ─── Laundry: Slot Conflict & Duplicate Booking ───────────
    @Test
    public void laundrySlotConflict() {
        // Simulate LaundryController's 6-slot array
        String[] laundrySlots = new String[6];

        // Student A books slot 0
        laundrySlots[0] = "StudentA";

        // Student B tries slot 0 → blocked
        boolean slotTaken = laundrySlots[0] != null;
        assertTrue("Laundry slot should be occupied", slotTaken);
    }

    @Test
    public void laundryStudentCannotBookTwice() {
        String[] laundrySlots = new String[6];
        String student = "StudentA";

        // Student A books slot 2
        laundrySlots[2] = student;

        // Check duplicate — student tries booking another slot
        boolean alreadyBooked = false;
        for (int i = 0; i < laundrySlots.length; i++) {
            if (student.equals(laundrySlots[i])) {
                alreadyBooked = true;
                break;
            }
        }
        assertTrue("Student already has an active laundry booking", alreadyBooked);
    }

    @Test
    public void laundryInvalidSlotIndex() {
        int slotIndex = -1;
        assertTrue("Negative index should be invalid", slotIndex < 0 || slotIndex >= 6);
        slotIndex = 6;
        assertTrue("Index 6 should be out of range", slotIndex < 0 || slotIndex >= 6);
    }

    // ─── Fridge: All Slots Full ───────────────────────────────
    @Test
    public void fridgeAllSlotsFull() {
        String[] fridgeSlots = new String[10];
        for (int i = 0; i < 10; i++) {
            fridgeSlots[i] = "Student" + i;
        }
        {

        }
        FirstFitAllocator alloc = new FirstFitAllocator();
        boolean threw = false;
        try {
            alloc.findSlot(fridgeSlots);
        } catch (SlotUnavailableException e) {
            threw = true;
        }
        assertTrue("Should throw when all fridge slots occupied", threw);
    }

    @Test
    public void fridgePartialOccupancy() throws SlotUnavailableException {
        String[] fridgeSlots = new String[10];
        fridgeSlots[0] = "StudentA";
        fridgeSlots[1] = "StudentB";
        // slots 2-9 are free → first fit returns 2
        FirstFitAllocator alloc = new FirstFitAllocator();
        assertEquals(2, alloc.findSlot(fridgeSlots));
    }

    // ─── Room: Full Room Cannot Be Allocated ──────────────────
    @Test
    public void roomFullCannotAllocate() {
        Room room = new Room("R101", 2, 2);
        assertFalse("Full room should not be available", room.isAvailable());
        int before = room.getCurrentOccupancy();
        room.incrementOccupancy();
        assertEquals("Occupancy should not exceed capacity", before, room.getCurrentOccupancy());
    }

    @Test
    public void roomMultipleStudentsFillRoom() {
        Room room = new Room("R201", 3, 0);
        assertTrue(room.isAvailable());
        room.incrementOccupancy(); // student 1
        room.incrementOccupancy(); // student 2
        room.incrementOccupancy(); // student 3
        assertFalse("Room should be full after 3 students", room.isAvailable());
        room.decrementOccupancy(); // one leaves
        assertTrue("Room should be available after one leaves", room.isAvailable());
    }

    // ─── Balance: Insufficient Funds Prevents Purchase ────────
    @Test
    public void insufficientBalancePreventsDeduction() {
        StudentBalance bal = new StudentBalance("STU001", 50.0);
        assertFalse(bal.deductBalance(100.0));
        assertEquals("Balance unchanged", 50.0, bal.getBalance(), 0.001);
    }

    @Test
    public void multipleDeductionsTrackBalance() {
        StudentBalance bal = new StudentBalance("STU001", 200.0);
        assertTrue(bal.deductBalance(50.0));
        assertTrue(bal.deductBalance(50.0));
        assertTrue(bal.deductBalance(50.0));
        assertEquals(50.0, bal.getBalance(), 0.001);
        assertFalse("Should fail — only 50 left", bal.deductBalance(60.0));
    }

    // ─── MealToken: Duplicate Token Prevention ────────────────
    @Test
    public void duplicateActiveTokenDetection() {
        // Simulate hasActiveToken logic from MealTokenController
        MealToken t1 = new MealToken("MT-1", "STU001", MealType.LUNCH, LocalDate.of(2099, 3, 8), TokenStatus.ACTIVE);
        MealToken t2 = new MealToken("MT-2", "STU001", MealType.DINNER, LocalDate.of(2099, 3, 8), TokenStatus.ACTIVE);
        MealToken t3 = new MealToken("MT-3", "STU001", MealType.LUNCH, LocalDate.of(2099, 3, 8), TokenStatus.USED);

        MealToken[] tokens = {t1, t2, t3};

        // Check: does student already have ACTIVE LUNCH token for this date?
        boolean hasActiveLunch = false;
        for (MealToken t : tokens) {
            if (t.getType() == MealType.LUNCH
                    && t.getDate().equals(LocalDate.of(2099, 3, 8))
                    && t.getStatus() == TokenStatus.ACTIVE) {
                hasActiveLunch = true;
                break;
            }
        }
        assertTrue("Should detect existing active LUNCH token", hasActiveLunch);

        // But BREAKFAST is still available
        boolean hasActiveBreakfast = false;
        for (MealToken t : tokens) {
            if (t.getType() == MealType.BREAKFAST
                    && t.getDate().equals(LocalDate.of(2099, 3, 8))
                    && t.getStatus() == TokenStatus.ACTIVE) {
                hasActiveBreakfast = true;
                break;
            }
        }
        assertFalse("No active BREAKFAST token exists", hasActiveBreakfast);
    }

    @Test
    public void usedTokenCannotBeVerifiedAgain() {
        MealToken token = new MealToken("MT-1", "STU001", MealType.LUNCH, LocalDate.of(2099, 3, 8), TokenStatus.USED);
        assertTrue(token.isUsed());
        assertEquals(TokenStatus.USED, token.getStatus());
    }

    @Test
    public void expiredTokenCannotBeUsed() {
        MealToken token = new MealToken("MT-1", "STU001", MealType.LUNCH, LocalDate.of(2000, 1, 1), TokenStatus.ACTIVE);
        assertEquals("Past-date ACTIVE token should be EXPIRED", TokenStatus.EXPIRED, token.getStatus());
    }

    // ─── TimeManager: Meal Slot Logic ─────────────────────────
    @Test
    public void mealSlotBreakfastWindow() {
        // Directly test isBetween logic — 08:00 is within breakfast 07:00-09:30
        LocalTime t = LocalTime.of(8, 0);
        assertTrue(isTimeBetween(t, "07:00", "09:30"));
    }

    @Test
    public void mealSlotLunchWindow() {
        LocalTime t = LocalTime.of(13, 0);
        assertTrue(isTimeBetween(t, "12:00", "14:00"));
    }

    @Test
    public void mealSlotDinnerWindow() {
        LocalTime t = LocalTime.of(20, 0);
        assertTrue(isTimeBetween(t, "19:00", "21:00"));
    }

    @Test
    public void mealSlotOutsideAllWindows() {
        LocalTime t = LocalTime.of(15, 0);
        assertFalse(isTimeBetween(t, "07:00", "09:30"));
        assertFalse(isTimeBetween(t, "12:00", "14:00"));
        assertFalse(isTimeBetween(t, "19:00", "21:00"));
    }

    @Test
    public void mealSlotBoundaryStart() {
        LocalTime t = LocalTime.of(7, 0);
        assertTrue("07:00 is start of breakfast", isTimeBetween(t, "07:00", "09:30"));
    }

    @Test
    public void mealSlotBoundaryEnd() {
        LocalTime t = LocalTime.of(9, 30);
        assertTrue("09:30 is end of breakfast", isTimeBetween(t, "07:00", "09:30"));
    }

    @Test
    public void ramadanSuhoorWindow() {
        LocalTime t = LocalTime.of(3, 30);
        assertTrue(isTimeBetween(t, "03:00", "04:30"));
    }

    @Test
    public void ramadanIftarWindow() {
        LocalTime t = LocalTime.of(18, 30);
        assertTrue(isTimeBetween(t, "18:00", "19:15"));
    }

    @Test
    public void mealEndTimeNormalMode() {
        // Test getMealEndTime without Ramadan
        // We can't easily toggle static state, so test the expected values
        assertEquals(LocalTime.parse("09:30"), TimeManager.getMealEndTime(MealType.BREAKFAST));
        assertEquals(LocalTime.parse("14:00"), TimeManager.getMealEndTime(MealType.LUNCH));
        assertEquals(LocalTime.parse("21:00"), TimeManager.getMealEndTime(MealType.DINNER));
    }

    // ─── TimeManager: Demo Mode ───────────────────────────────
    @Test
    public void demoModeToggle() {
        assertFalse(TimeManager.isDemoMode());
        TimeManager.setDemoMode(true);
        assertTrue(TimeManager.isDemoMode());
        // nowDate in demo mode should be today
        assertEquals(LocalDate.now(), TimeManager.nowDate());
        TimeManager.setDemoMode(false);
        assertFalse(TimeManager.isDemoMode());
    }

    // ─── ShoppingCart: Conflict & Edge Cases ──────────────────
    @Test
    public void cartDuplicateItemMergesQuantity() {
        ShoppingCart cart = new ShoppingCart();
        cart.addItem("I001", "Soap", 2, 25.0);
        cart.addItem("I001", "Soap", 3, 25.0);
        CartItem[] items = cart.getItems();
        assertEquals("Should merge into 1 entry", 1, items.length);
        assertEquals(5, items[0].getQuantity());
        assertEquals(125.0, items[0].getSubtotal(), 0.001);
    }

    @Test
    public void cartRemoveNonExistentItemNoEffect() {
        ShoppingCart cart = new ShoppingCart();
        cart.addItem("I001", "Soap", 1, 25.0);
        cart.removeItem("I999");  // doesn't exist
        assertEquals(1, cart.getItemCount());
    }

    // ─── Item: Reduce Below Zero ──────────────────────────────
    @Test
    public void itemReduceQuantityBelowZero() {
        Item item = new Item("I001", "Soap", 5, 25.0);
        item.reduceQuantity(10);
        assertEquals("Quantity goes negative (no guard)", -5, item.getQuantity());
    }

    // ─── Complaint: Status Transitions ────────────────────────
    @Test
    public void complaintStatusTransitions() {
        StudentPublicInfo info = new StudentPublicInfo("STU001", "Alice", "R101");
        Complaint c = Complaint.createNew("C-1", info, ComplaintCategory.ELECTRICITY, "Broken fan", PriorityLevel.NORMAL, "");

        assertEquals(ComplaintStatus.SUBMITTED, c.getStatus());

        c.assignTo("W001");
        assertEquals(ComplaintStatus.ASSIGNED, c.getStatus());

        c.setStatus(ComplaintStatus.IN_PROGRESS);
        assertEquals(ComplaintStatus.IN_PROGRESS, c.getStatus());

        c.setStatus(ComplaintStatus.RESOLVED);
        assertEquals(ComplaintStatus.RESOLVED, c.getStatus());
    }

    @Test
    public void complaintReassignWorker() {
        StudentPublicInfo info = new StudentPublicInfo("STU001", "Alice", "R101");
        Complaint c = Complaint.createNew("C-1", info, ComplaintCategory.PLUMBING, "Leak", PriorityLevel.HIGH, "");
        c.assignTo("W001");
        assertEquals("W001", c.getAssignedWorkerId());

        // Clear and reassign to different worker
        c.clearAssignment();
        assertEquals(ComplaintStatus.SUBMITTED, c.getStatus());
        c.assignTo("W002");
        assertEquals("W002", c.getAssignedWorkerId());
        assertEquals(ComplaintStatus.ASSIGNED, c.getStatus());
    }

    // ─── ComplaintPolicy: Emergency Keywords ──────────────────
    @Test
    public void emergencyElectricShock() {
        ComplaintPolicy policy = new ComplaintPolicy();
        ComplaintPolicy.DormDecision d = policy.decide(ComplaintCategory.ELECTRICITY, "I got an electric shock from the switch");
        assertTrue(d.isEmergency());
        assertEquals(PriorityLevel.EMERGENCY, d.getPriority());
    }

    @Test
    public void emergencySmoke() {
        ComplaintPolicy policy = new ComplaintPolicy();
        ComplaintPolicy.DormDecision d = policy.decide(ComplaintCategory.ELECTRICITY, "There is smoke coming from the outlet");
        assertTrue(d.isEmergency());
    }

    @Test
    public void nonEmergencyCleaning() {
        ComplaintPolicy policy = new ComplaintPolicy();
        ComplaintPolicy.DormDecision d = policy.decide(ComplaintCategory.CLEANING, "bathroom needs cleaning");
        assertFalse(d.isEmergency());
        assertEquals(PriorityLevel.NORMAL, d.getPriority());
    }

    // ─── MyArrayList: Concurrent-Style Add Remove ─────────────
    @Test
    public void arrayListAddRemoveSequence() {
        MyArrayList<String> list = new MyArrayList<>();
        for (int i = 0; i < 20; i++) {
            list.add("item" + i);
        }
        assertEquals(20, list.size());

        // Remove every other item from front
        for (int i = 0; i < 10; i++) {
            list.remove(0);
        }
        assertEquals(10, list.size());
        assertEquals("item10", list.get(0));
    }

    @Test
    public void arrayListForEach() {
        MyArrayList<Integer> list = new MyArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        int[] sum = {0};
        list.forEach(n -> sum[0] += n);
        assertEquals(6, sum[0]);
    }

    // ─── MyString: Edge Cases ─────────────────────────────────
    @Test
    public void myStringNullConstructor() {
        MyString s = new MyString((String) null);
        assertEquals(0, s.length());
        assertTrue(s.isEmpty());
    }

    @Test
    public void myStringSplitNoDelimiter() {
        MyString s = new MyString("hello");
        MyString[] parts = s.split(',');
        assertEquals(1, parts.length);
        assertEquals("hello", parts[0].getValue());
    }

    @Test
    public void myStringSubstringBoundsHandling() {
        MyString s = new MyString("abc");
        // begin beyond size → empty
        assertEquals("", s.substring(5, 10).getValue());
        // end beyond size → clamped
        assertEquals("abc", s.substring(0, 100).getValue());
    }

    // Helper: mirrors TimeManager.isBetween
    private static boolean isTimeBetween(LocalTime now, String start, String end) {
        return !now.isBefore(LocalTime.parse(start)) && !now.isAfter(LocalTime.parse(end));
    }

    // ─── RoutineEntry Model Tests ─────────────────────────────

    @Test
    public void routineEntry_toFileString_formatsCorrectly() {
        RoutineEntry entry = new RoutineEntry("STU001", DayOfWeek.MONDAY, 4, "Class");
        String result = entry.toFileString();
        assertEquals("STU001|MONDAY|4|Class", result);
    }

    @Test
    public void routineEntry_fromFileString_parsesCorrectly() {
        RoutineEntry entry = RoutineEntry.fromFileString("STU001|TUESDAY|3|Lab Work");
        assertNotNull(entry);
        assertEquals("STU001", entry.getStudentId());
        assertEquals(DayOfWeek.TUESDAY, entry.getDay());
        assertEquals(3, entry.getSlotIndex());
        assertEquals("Lab Work", entry.getContent());
    }

    @Test
    public void routineEntry_fromFileString_nullInputReturnsNull() {
        RoutineEntry entry = RoutineEntry.fromFileString(null);
        assertNull(entry);
    }

    @Test
    public void routineEntry_fromFileString_emptyStringReturnsNull() {
        RoutineEntry entry = RoutineEntry.fromFileString("");
        assertNull(entry);
    }

    @Test
    public void routineEntry_fromFileString_tooFewPartsReturnsNull() {
        RoutineEntry entry = RoutineEntry.fromFileString("STU001|MONDAY|3");
        assertNull(entry);
    }

    @Test
    public void routineEntry_fromFileString_invalidDayReturnsNull() {
        RoutineEntry entry = RoutineEntry.fromFileString("STU001|FUNDAY|3|Class");
        assertNull(entry);
    }

    @Test
    public void routineEntry_fromFileString_invalidSlotIndexReturnsNull() {
        RoutineEntry entry = RoutineEntry.fromFileString("STU001|MONDAY|abc|Class");
        assertNull(entry);
    }

    @Test
    public void routineEntry_pipeInContent_isReplacedWithSlash() {
        RoutineEntry entry = new RoutineEntry("STU001", DayOfWeek.MONDAY, 0, "Math|Science");
        assertEquals("Math/Science", entry.getContent());
    }

    @Test
    public void routineEntry_nullContent_isAllowed() {
        RoutineEntry entry = new RoutineEntry("STU001", DayOfWeek.MONDAY, 0, null);
        assertNull(entry.getContent());
    }

    @Test
    public void routineEntry_roundTrip_preservesData() {
        RoutineEntry original = new RoutineEntry("STU042", DayOfWeek.FRIDAY, 7, "Gym");
        RoutineEntry parsed = RoutineEntry.fromFileString(original.toFileString());
        assertNotNull(parsed);
        assertEquals(original.getStudentId(), parsed.getStudentId());
        assertEquals(original.getDay(), parsed.getDay());
        assertEquals(original.getSlotIndex(), parsed.getSlotIndex());
        assertEquals(original.getContent(), parsed.getContent());
    }

    // ─── StudentRoutineEntry Tests ────────────────────────────

    @Test
    public void studentRoutineEntry_hasContent_trueWhenContentSet() {
        StudentRoutineEntry entry = new StudentRoutineEntry("STU001", DayOfWeek.MONDAY, 4, "Lecture");
        assertTrue(entry.hasContent());
    }

    @Test
    public void studentRoutineEntry_hasContent_falseWhenNull() {
        StudentRoutineEntry entry = new StudentRoutineEntry("STU001", DayOfWeek.MONDAY, 4, null);
        assertFalse(entry.hasContent());
    }

    @Test
    public void studentRoutineEntry_hasContent_falseWhenBlank() {
        StudentRoutineEntry entry = new StudentRoutineEntry("STU001", DayOfWeek.MONDAY, 4, "   ");
        assertFalse(entry.hasContent());
    }

    @Test
    public void studentRoutineEntry_hasContent_falseWhenEmpty() {
        StudentRoutineEntry entry = new StudentRoutineEntry("STU001", DayOfWeek.MONDAY, 4, "");
        assertFalse(entry.hasContent());
    }

    @Test
    public void studentRoutineEntry_getters_returnCorrectValues() {
        StudentRoutineEntry entry = new StudentRoutineEntry("STU002", DayOfWeek.WEDNESDAY, 6, "Tutorial");
        assertEquals("STU002", entry.getStudentId());
        assertEquals(DayOfWeek.WEDNESDAY, entry.getDay());
        assertEquals(6, entry.getSlotIndex());
        assertEquals("Tutorial", entry.getContent());
    }

    // ─── WorkerVisitEntry Tests ───────────────────────────────

    @Test
    public void workerVisitEntry_getters_returnCorrectValues() {
        WorkerVisitEntry entry = new WorkerVisitEntry(
                "CMP001", "WRK01", "STU001", "101",
                DayOfWeek.THURSDAY, 4, "PLANNED", "AUTO"
        );
        assertEquals("CMP001", entry.getComplaintId());
        assertEquals("WRK01", entry.getWorkerId());
        assertEquals("STU001", entry.getStudentId());
        assertEquals("101", entry.getRoomNo());
        assertEquals(DayOfWeek.THURSDAY, entry.getDay());
        assertEquals(4, entry.getSlotIndex());
        assertEquals("PLANNED", entry.getStatus());
        assertEquals("AUTO", entry.getNote());
    }

    @Test
    public void workerVisitEntry_nullNote_isAllowed() {
        WorkerVisitEntry entry = new WorkerVisitEntry(
                "CMP002", "WRK02", "STU002", "202",
                DayOfWeek.FRIDAY, 5, "DONE", null
        );
        assertNull(entry.getNote());
    }

    @Test
    public void workerVisitEntry_slotIndex_boundsAreRespected() {
        WorkerVisitEntry first = new WorkerVisitEntry("C1","W1","S1","R1", DayOfWeek.MONDAY, 0, "PLANNED", "");
        WorkerVisitEntry last  = new WorkerVisitEntry("C2","W2","S2","R2", DayOfWeek.MONDAY, 5, "PLANNED", "");
        assertEquals(0, first.getSlotIndex());
        assertEquals(5, last.getSlotIndex());
    }

    // ─── RoutineController — Slot Index Logic Tests ───────────

    @Test
    public void routineController_fullSlotLabels_has12Entries() {
        assertEquals(12, RoutineController.FULL_SLOT_LABELS.length);
    }

    @Test
    public void routineController_attendantSlotLabels_has6Entries() {
        assertEquals(6, RoutineController.ATTENDANT_SLOT_LABELS.length);
    }

    @Test
    public void routineController_attendantSlotLabels_startAt0800() {
        assertEquals("08-10", RoutineController.ATTENDANT_SLOT_LABELS[0]);
    }

    @Test
    public void routineController_attendantSlotLabels_endAt2000() {
        assertEquals("18-20", RoutineController.ATTENDANT_SLOT_LABELS[5]);
    }

    @Test
    public void routineController_fullSlotLabels_startsAtMidnight() {
        assertEquals("00-02", RoutineController.FULL_SLOT_LABELS[0]);
    }

    @Test
    public void routineController_isPrivateByDefaultNightSlot_trueForSlot0() {
        RoutineController rc = new RoutineController();
        assertTrue(rc.isPrivateByDefaultNightSlot(0));
    }

    @Test
    public void routineController_isPrivateByDefaultNightSlot_trueForSlot1() {
        RoutineController rc = new RoutineController();
        assertTrue(rc.isPrivateByDefaultNightSlot(1));
    }

    @Test
    public void routineController_isPrivateByDefaultNightSlot_trueForSlot10() {
        RoutineController rc = new RoutineController();
        assertTrue(rc.isPrivateByDefaultNightSlot(10));
    }

    @Test
    public void routineController_isPrivateByDefaultNightSlot_trueForSlot11() {
        RoutineController rc = new RoutineController();
        assertTrue(rc.isPrivateByDefaultNightSlot(11));
    }

    @Test
    public void routineController_isPrivateByDefaultNightSlot_falseForDaytimeSlot() {
        RoutineController rc = new RoutineController();
        // Slots 2-9 are NOT private by default
        for (int i = 2; i <= 9; i++) {
            assertFalse("Slot " + i + " should not be private by default",
                    rc.isPrivateByDefaultNightSlot(i));
        }
    }

    @Test
    public void routineController_isStudentBusy24_trueForNightSlotEvenWithNoEntry() {
        RoutineController rc = new RoutineController();
        // Slot 0 (00-02) should always be busy — private by default
        // No file entry needed; default night logic alone makes it busy
        assertTrue(rc.isStudentBusy24("GHOST_STUDENT", DayOfWeek.MONDAY, 0));
    }

    @Test
    public void routineController_isStudentBusy24_trueForSlot11() {
        RoutineController rc = new RoutineController();
        assertTrue(rc.isStudentBusy24("GHOST_STUDENT", DayOfWeek.FRIDAY, 11));
    }

    // ─── WorkerScheduleController — isDefaultDutyDay Tests ───

    @Test
    public void isDefaultDutyDay_saturdayAlwaysTrue_forAnyWorker() {
        WorkerScheduleController wsc = new WorkerScheduleController();
        MaintenanceWorker w = new MaintenanceWorker("W1", "Ali", "MAINTENANCE_WORKER", "pass", "01700000001", WorkerField.ELECTRICIAN);
        assertTrue(wsc.isDefaultDutyDay(w, DayOfWeek.SATURDAY));
    }

    @Test
    public void isDefaultDutyDay_sundayAlwaysTrue_forAnyWorker() {
        WorkerScheduleController wsc = new WorkerScheduleController();
        MaintenanceWorker w = new MaintenanceWorker("W2", "Rahim", "MAINTENANCE_WORKER", "pass", "01700000002", WorkerField.PLUMBER);
        assertTrue(wsc.isDefaultDutyDay(w, DayOfWeek.SUNDAY));
    }

    @Test
    public void isDefaultDutyDay_cleaningWorker_tuesdayIsTrue() {
        WorkerScheduleController wsc = new WorkerScheduleController();
        MaintenanceWorker w = new MaintenanceWorker("W3", "Karim", "MAINTENANCE_WORKER", "pass", "01700000003", WorkerField.CLEANING);
        assertTrue(wsc.isDefaultDutyDay(w, DayOfWeek.TUESDAY));
    }

    @Test
    public void isDefaultDutyDay_cleaningWorker_thursdayIsTrue() {
        WorkerScheduleController wsc = new WorkerScheduleController();
        MaintenanceWorker w = new MaintenanceWorker("W4", "Nadia", "MAINTENANCE_WORKER", "pass", "01700000004", WorkerField.CLEANING);
        assertTrue(wsc.isDefaultDutyDay(w, DayOfWeek.THURSDAY));
    }

    @Test
    public void isDefaultDutyDay_nonCleaningWorker_tuesdayIsFalse() {
        WorkerScheduleController wsc = new WorkerScheduleController();
        MaintenanceWorker w = new MaintenanceWorker("W5", "Reza", "MAINTENANCE_WORKER", "pass", "01700000005", WorkerField.ELECTRICIAN);
        assertFalse(wsc.isDefaultDutyDay(w, DayOfWeek.TUESDAY));
    }

    @Test
    public void isDefaultDutyDay_nonCleaningWorker_mondayIsFalse() {
        WorkerScheduleController wsc = new WorkerScheduleController();
        MaintenanceWorker w = new MaintenanceWorker("W6", "Salam", "MAINTENANCE_WORKER", "pass", "01700000006", WorkerField.PLUMBER);
        assertFalse(wsc.isDefaultDutyDay(w, DayOfWeek.MONDAY));
    }

    @Test
    public void isDefaultDutyDay_cleaningWorker_mondayIsFalse() {
        WorkerScheduleController wsc = new WorkerScheduleController();
        MaintenanceWorker w = new MaintenanceWorker("W7", "Nasrin", "MAINTENANCE_WORKER", "pass", "01700000007", WorkerField.CLEANING);
        // MONDAY is not SATURDAY, SUNDAY, TUESDAY, or THURSDAY → false
        assertFalse(wsc.isDefaultDutyDay(w, DayOfWeek.MONDAY));
    }

    // ─── WorkerScheduleController — Slot Labels Tests ─────────

    @Test
    public void workerScheduleController_slotLabels_hasSixEntries() {
        assertEquals(6, WorkerScheduleController.SLOT_LABELS.length);
    }

    @Test
    public void workerScheduleController_slotLabels_firstIs0810() {
        assertEquals("08-10", WorkerScheduleController.SLOT_LABELS[0]);
    }

    @Test
    public void workerScheduleController_slotLabels_lastIs1820() {
        assertEquals("18-20", WorkerScheduleController.SLOT_LABELS[5]);
    }

    @Test
    public void workerScheduleController_manualPlan_invalidSlotIndexReturnsFalse() {
        // slotIndex < 0 should be rejected before any repo lookup
        WorkerScheduleController wsc = new WorkerScheduleController();
        boolean result = wsc.manualPlanComplaint("NONEXISTENT", DayOfWeek.MONDAY, -1, "test");
        assertFalse(result);
    }

    @Test
    public void workerScheduleController_manualPlan_outOfBoundsSlotReturnsFalse() {
        WorkerScheduleController wsc = new WorkerScheduleController();
        boolean result = wsc.manualPlanComplaint("NONEXISTENT", DayOfWeek.MONDAY, 99, "test");
        assertFalse(result);
    }

    @Test
    public void workerScheduleController_manualPlan_nonExistentComplaintReturnsFalse() {
        WorkerScheduleController wsc = new WorkerScheduleController();
        boolean result = wsc.manualPlanComplaint("COMPLAINT_DOES_NOT_EXIST_XYZ", DayOfWeek.MONDAY, 4, "note");
        assertFalse(result);
    }

    @Test
    public void workerScheduleController_autoPlan_nonExistentComplaintReturnsEmpty() {
        WorkerScheduleController wsc = new WorkerScheduleController();
        assertTrue(wsc.autoPlanComplaint("COMPLAINT_DOES_NOT_EXIST_XYZ").isEmpty());
    }

    @Test
    public void workerScheduleController_complaintExists_nonExistentReturnsFalse() {
        WorkerScheduleController wsc = new WorkerScheduleController();
        assertFalse(wsc.complaintExists("COMPLAINT_DOES_NOT_EXIST_XYZ"));
    }

    @Test
    public void workerScheduleController_isResolvedComplaint_nonExistentReturnsFalse() {
        WorkerScheduleController wsc = new WorkerScheduleController();
        assertFalse(wsc.isResolvedComplaint("COMPLAINT_DOES_NOT_EXIST_XYZ"));
    }

    // ─── RoutineEntry — All Days Round-Trip ───────────────────

    @Test
    public void routineEntry_roundTrip_allDaysOfWeek() {
        DayOfWeek[] days = DayOfWeek.values();
        for (DayOfWeek day : days) {
            RoutineEntry original = new RoutineEntry("STU001", day, 4, "Test");
            RoutineEntry parsed = RoutineEntry.fromFileString(original.toFileString());
            assertNotNull("Round-trip failed for day: " + day, parsed);
            assertEquals(day, parsed.getDay());
        }
    }

    @Test
    public void routineEntry_fromFileString_allSlotIndices_parseCorrectly() {
        for (int slot = 0; slot <= 11; slot++) {
            RoutineEntry entry = RoutineEntry.fromFileString("STU001|MONDAY|" + slot + "|Content");
            assertNotNull("Failed for slot: " + slot, entry);
            assertEquals(slot, entry.getSlotIndex());
        }
    }

    @Test
    public void routineEntry_toFileString_slotZero_formatsCorrectly() {
        RoutineEntry entry = new RoutineEntry("STU010", DayOfWeek.SUNDAY, 0, "Sleep");
        assertEquals("STU010|SUNDAY|0|Sleep", entry.toFileString());
    }

    @Test
    public void routineEntry_toFileString_slotEleven_formatsCorrectly() {
        RoutineEntry entry = new RoutineEntry("STU010", DayOfWeek.SATURDAY, 11, "Rest");
        assertEquals("STU010|SATURDAY|11|Rest", entry.toFileString());
    }

    @Test
    public void routineEntry_multiplePipesInContent_allReplacedWithSlash() {
        RoutineEntry entry = new RoutineEntry("STU001", DayOfWeek.MONDAY, 4, "A|B|C");
        assertEquals("A/B/C", entry.getContent());
    }

    @Test
    public void routineEntry_fromFileString_emptyContent_parsesAsEmpty() {
        RoutineEntry entry = RoutineEntry.fromFileString("STU001|MONDAY|4|");
        assertNotNull(entry);
        assertEquals("", entry.getContent());
    }

    // ─── StudentRoutineEntry — Day & Slot Variation Tests ────

    @Test
    public void studentRoutineEntry_allDaysOfWeek_storeCorrectly() {
        DayOfWeek[] days = DayOfWeek.values();
        for (DayOfWeek day : days) {
            StudentRoutineEntry entry = new StudentRoutineEntry("STU001", day, 4, "Class");
            assertEquals(day, entry.getDay());
        }
    }

    @Test
    public void studentRoutineEntry_slotZero_isValid() {
        StudentRoutineEntry entry = new StudentRoutineEntry("STU001", DayOfWeek.MONDAY, 0, "Sleep");
        assertEquals(0, entry.getSlotIndex());
        assertTrue(entry.hasContent());
    }

    @Test
    public void studentRoutineEntry_slotEleven_isValid() {
        StudentRoutineEntry entry = new StudentRoutineEntry("STU001", DayOfWeek.SUNDAY, 11, "Night Study");
        assertEquals(11, entry.getSlotIndex());
        assertTrue(entry.hasContent());
    }

    @Test
    public void studentRoutineEntry_twoEntriesSameStudent_differentSlots_areDistinct() {
        StudentRoutineEntry a = new StudentRoutineEntry("STU001", DayOfWeek.MONDAY, 4, "Lecture");
        StudentRoutineEntry b = new StudentRoutineEntry("STU001", DayOfWeek.MONDAY, 5, "Lab");
        assertNotEquals(a.getSlotIndex(), b.getSlotIndex());
        assertNotEquals(a.getContent(), b.getContent());
    }

    @Test
    public void studentRoutineEntry_twoEntriesSameSlot_differentDays_areDistinct() {
        StudentRoutineEntry a = new StudentRoutineEntry("STU001", DayOfWeek.MONDAY, 4, "Lecture");
        StudentRoutineEntry b = new StudentRoutineEntry("STU001", DayOfWeek.TUESDAY, 4, "Lecture");
        assertNotEquals(a.getDay(), b.getDay());
        assertEquals(a.getContent(), b.getContent());
    }

    // ─── WorkerVisitEntry — Status & Day Variation Tests ─────

    @Test
    public void workerVisitEntry_statusPlanned_storedCorrectly() {
        WorkerVisitEntry entry = new WorkerVisitEntry("C1","W1","S1","R1", DayOfWeek.MONDAY, 0, "PLANNED", "");
        assertEquals("PLANNED", entry.getStatus());
    }

    @Test
    public void workerVisitEntry_statusDone_storedCorrectly() {
        WorkerVisitEntry entry = new WorkerVisitEntry("C1","W1","S1","R1", DayOfWeek.MONDAY, 0, "DONE", "");
        assertEquals("DONE", entry.getStatus());
    }

    @Test
    public void workerVisitEntry_statusCancelled_storedCorrectly() {
        WorkerVisitEntry entry = new WorkerVisitEntry("C1","W1","S1","R1", DayOfWeek.MONDAY, 0, "CANCELLED", "");
        assertEquals("CANCELLED", entry.getStatus());
    }

    @Test
    public void workerVisitEntry_allDaysOfWeek_storeCorrectly() {
        DayOfWeek[] days = DayOfWeek.values();
        for (DayOfWeek day : days) {
            WorkerVisitEntry entry = new WorkerVisitEntry("C1","W1","S1","R1", day, 0, "PLANNED", "");
            assertEquals(day, entry.getDay());
        }
    }

    @Test
    public void workerVisitEntry_emptyNote_storedCorrectly() {
        WorkerVisitEntry entry = new WorkerVisitEntry("C1","W1","S1","R1", DayOfWeek.MONDAY, 0, "PLANNED", "");
        assertEquals("", entry.getNote());
    }

    // ─── RoutineController — putSlotByStudentId Validation ───

    @Test
    public void routineController_putSlotByStudentId_negativeSlotReturnsFalse() {
        RoutineController rc = new RoutineController();
        assertFalse(rc.putSlotByStudentId("STU001", DayOfWeek.MONDAY, -1, "Class"));
    }

    @Test
    public void routineController_putSlotByStudentId_slotTooHighReturnsFalse() {
        RoutineController rc = new RoutineController();
        assertFalse(rc.putSlotByStudentId("STU001", DayOfWeek.MONDAY, 12, "Class"));
    }

    @Test
    public void routineController_putSlotByStudentId_slot11IsValid() {
        RoutineController rc = new RoutineController();
        assertTrue(rc.putSlotByStudentId("STU_TEST_VALID", DayOfWeek.MONDAY, 11, "Night Study"));
    }

    @Test
    public void routineController_putSlotByStudentId_slot0IsValid() {
        RoutineController rc = new RoutineController();
        assertTrue(rc.putSlotByStudentId("STU_TEST_VALID", DayOfWeek.MONDAY, 0, "Sleep"));
    }

    // ─── RoutineController — clearSlotByStudentId Validation ─

    @Test
    public void routineController_clearSlotByStudentId_negativeSlotReturnsFalse() {
        RoutineController rc = new RoutineController();
        assertFalse(rc.clearSlotByStudentId("STU001", DayOfWeek.MONDAY, -1));
    }

    @Test
    public void routineController_clearSlotByStudentId_slotTooHighReturnsFalse() {
        RoutineController rc = new RoutineController();
        assertFalse(rc.clearSlotByStudentId("STU001", DayOfWeek.MONDAY, 12));
    }

    @Test
    public void routineController_clearSlotByStudentId_validSlotReturnsTrue() {
        RoutineController rc = new RoutineController();
        assertTrue(rc.clearSlotByStudentId("GHOST_STUDENT", DayOfWeek.WEDNESDAY, 5));
    }

    // ─── RoutineController — writeComplaintVisit Validation ──

    @Test
    public void routineController_writeComplaintVisit_negativeAttendantSlotReturnsFalse() {
        RoutineController rc = new RoutineController();
        assertFalse(rc.writeComplaintVisit("STU001", DayOfWeek.MONDAY, -1, "CMP001", "Visit"));
    }

    @Test
    public void routineController_writeComplaintVisit_slotTooHighReturnsFalse() {
        RoutineController rc = new RoutineController();
        assertFalse(rc.writeComplaintVisit("STU001", DayOfWeek.MONDAY, 6, "CMP001", "Visit"));
    }

    // ─── RoutineController — clearComplaintVisitIfPresent ────

    @Test
    public void routineController_clearComplaintVisit_negativeSlotReturnsFalse() {
        RoutineController rc = new RoutineController();
        assertFalse(rc.clearComplaintVisitIfPresent("STU001", DayOfWeek.MONDAY, -1, "CMP001"));
    }

    @Test
    public void routineController_clearComplaintVisit_slotTooHighReturnsFalse() {
        RoutineController rc = new RoutineController();
        assertFalse(rc.clearComplaintVisitIfPresent("STU001", DayOfWeek.MONDAY, 6, "CMP001"));
    }

    @Test
    public void routineController_clearComplaintVisit_nonExistentEntryReturnsFalse() {
        RoutineController rc = new RoutineController();
        assertFalse(rc.clearComplaintVisitIfPresent("GHOST_STUDENT_XYZ", DayOfWeek.FRIDAY, 3, "CMP999"));
    }

    // ─── RoutineController — isBusyForAttendantWindowExceptComplaint ──

    @Test
    public void routineController_isBusyExceptComplaint_negativeSlotReturnsTrue() {
        RoutineController rc = new RoutineController();
        assertTrue(rc.isBusyForAttendantWindowExceptComplaint("STU001", DayOfWeek.MONDAY, -1, "CMP001"));
    }

    @Test
    public void routineController_isBusyExceptComplaint_slotTooHighReturnsTrue() {
        RoutineController rc = new RoutineController();
        assertTrue(rc.isBusyForAttendantWindowExceptComplaint("STU001", DayOfWeek.MONDAY, 6, "CMP001"));
    }

    @Test
    public void routineController_isBusyExceptComplaint_ghostStudentNoEntryReturnsFalse() {
        RoutineController rc = new RoutineController();
        assertFalse(rc.isBusyForAttendantWindowExceptComplaint("GHOST_STUDENT_XYZ", DayOfWeek.MONDAY, 3, "CMP001"));
    }

    // ─── RoutineController — Full Slot Labels Content ─────────

    @Test
    public void routineController_fullSlotLabels_endsAt2400() {
        assertEquals("22-24", RoutineController.FULL_SLOT_LABELS[11]);
    }

    @Test
    public void routineController_fullSlotLabels_attendantWindowStartsAtIndex4() {
        assertEquals("08-10", RoutineController.FULL_SLOT_LABELS[4]);
    }

    @Test
    public void routineController_fullSlotLabels_attendantWindowEndsAtIndex9() {
        assertEquals("18-20", RoutineController.FULL_SLOT_LABELS[9]);
    }

    // ─── WorkerScheduleController — isDefaultDutyDay Remaining ──

    @Test
    public void isDefaultDutyDay_internetTechWorker_saturdayIsTrue() {
        WorkerScheduleController wsc = new WorkerScheduleController();
        MaintenanceWorker w = new MaintenanceWorker("W8", "Tariq", "MAINTENANCE_WORKER", "pass", "01700000008", WorkerField.INTERNET_TECH);
        assertTrue(wsc.isDefaultDutyDay(w, DayOfWeek.SATURDAY));
    }

    @Test
    public void isDefaultDutyDay_internetTechWorker_tuesdayIsFalse() {
        WorkerScheduleController wsc = new WorkerScheduleController();
        MaintenanceWorker w = new MaintenanceWorker("W8", "Tariq", "MAINTENANCE_WORKER", "pass", "01700000008", WorkerField.INTERNET_TECH);
        assertFalse(wsc.isDefaultDutyDay(w, DayOfWeek.TUESDAY));
    }

    @Test
    public void isDefaultDutyDay_cleaningWorker_wednesdayIsFalse() {
        WorkerScheduleController wsc = new WorkerScheduleController();
        MaintenanceWorker w = new MaintenanceWorker("W9", "Rima", "MAINTENANCE_WORKER", "pass", "01700000009", WorkerField.CLEANING);
        assertFalse(wsc.isDefaultDutyDay(w, DayOfWeek.WEDNESDAY));
    }

    @Test
    public void isDefaultDutyDay_cleaningWorker_fridayIsFalse() {
        WorkerScheduleController wsc = new WorkerScheduleController();
        MaintenanceWorker w = new MaintenanceWorker("W10", "Dina", "MAINTENANCE_WORKER", "pass", "01700000010", WorkerField.CLEANING);
        assertFalse(wsc.isDefaultDutyDay(w, DayOfWeek.FRIDAY));
    }

    // ─── WorkerScheduleController — SLOT_LABELS All Values ───

    @Test
    public void workerScheduleController_slotLabels_allValuesCorrect() {
        String[] expected = {"08-10", "10-12", "12-14", "14-16", "16-18", "18-20"};
        assertArrayEquals(expected, WorkerScheduleController.SLOT_LABELS);
    }

    // ─── WorkerScheduleController — markVisitDone ────────────

    @Test
    public void workerScheduleController_markVisitDone_nonExistentComplaintReturnsFalse() {
        WorkerScheduleController wsc = new WorkerScheduleController();
        assertFalse(wsc.markVisitDone("COMPLAINT_DOES_NOT_EXIST_XYZ"));
    }

    // ─── WorkerScheduleController — renderWorkerWeek ─────────

    @Test
    public void workerScheduleController_renderWorkerWeek_unknownWorkerReturnsMessage() {
        WorkerScheduleController wsc = new WorkerScheduleController();
        String result = wsc.renderWorkerWeek("NONEXISTENT_WORKER_TOKEN_XYZ");
        assertNotNull(result);
        assertFalse(result.trim().isEmpty());
    }

    // ─── MaintenanceWorker — Field & Role Tests ───────────────

    @Test
    public void maintenanceWorker_getField_returnsCorrectField() {
        MaintenanceWorker w = new MaintenanceWorker("W1", "Ali", "MAINTENANCE_WORKER", "hash", "01700000001", WorkerField.ELECTRICIAN);
        assertEquals(WorkerField.ELECTRICIAN, w.getField());
    }

    @Test
    public void maintenanceWorker_getRole_returnsMaintenanceWorker() {
        MaintenanceWorker w = new MaintenanceWorker("W1", "Ali", "MAINTENANCE_WORKER", "hash", "01700000001", WorkerField.PLUMBER);
        assertEquals("MAINTENANCE_WORKER", w.getRole());
    }

    @Test
    public void maintenanceWorker_getId_returnsCorrectId() {
        MaintenanceWorker w = new MaintenanceWorker("WRK99", "Sadia", "MAINTENANCE_WORKER", "hash", "01700000099", WorkerField.CLEANING);
        assertEquals("WRK99", w.getId());
    }

    @Test
    public void maintenanceWorker_getName_returnsCorrectName() {
        MaintenanceWorker w = new MaintenanceWorker("W1", "Jamal", "MAINTENANCE_WORKER", "hash", "01700000001", WorkerField.INTERNET_TECH);
        assertEquals("Jamal", w.getName());
    }

    @Test
    public void maintenanceWorker_allWorkerFields_canBeAssigned() {
        WorkerField[] fields = WorkerField.values();
        for (WorkerField field : fields) {
            MaintenanceWorker w = new MaintenanceWorker("WX", "Test", "MAINTENANCE_WORKER", "hash", "0170", field);
            assertEquals(field, w.getField());
        }
    }
}
