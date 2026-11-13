package ph.gov.santarosa.petcare.web;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ph.gov.santarosa.petcare.domain.*;
import ph.gov.santarosa.petcare.repository.*;
import ph.gov.santarosa.petcare.security.CurrentUser;
import ph.gov.santarosa.petcare.service.Access;
import ph.gov.santarosa.petcare.service.Values;

/** Administrator workbook for reporting/offline review. Database disaster recovery remains a hosting backup. */
@RestController
@RequestMapping("/api/admin/export")
public class AdminExportController {
    private final CurrentUser current; private final AppUserRepository users; private final PetRepository pets;
    private final PetRegistrationRepository registrations; private final ServiceRequestRepository requests;
    private final AdoptionApplicationRepository adoptions; private final AuditLogRepository audit;

    public AdminExportController(CurrentUser current, AppUserRepository users, PetRepository pets,
            PetRegistrationRepository registrations, ServiceRequestRepository requests,
            AdoptionApplicationRepository adoptions, AuditLogRepository audit) {
        this.current=current; this.users=users; this.pets=pets; this.registrations=registrations;
        this.requests=requests; this.adoptions=adoptions; this.audit=audit;
    }

    @GetMapping("/workbook")
    public void workbook(HttpServletResponse response) throws IOException {
        Access.require(current.requireReady(), Values.ADMIN);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=dogcare-data-" + LocalDate.now() + ".xlsx");
        try (Workbook wb = new XSSFWorkbook()) {
            add(wb,"Users",List.of("ID","Name","Email","Role","Barangay","Status"),users.findAll(),
                    u->new Object[]{u.getId(),u.getFullName(),u.getEmail(),u.getRole(),u.getBarangay(),u.getStatus()});
            add(wb,"Dogs",List.of("ID","Owner ID","Name","Breed","Sex","Birth date","Barangay","Verification","Archived"),pets.findAll(),
                    p->new Object[]{p.getId(),p.getOwnerId(),p.getName(),p.getBreed(),p.getSex(),p.getDob(),p.getBarangay(),p.getRegistrationStatus(),p.getArchived()});
            add(wb,"Registrations",List.of("ID","Dog ID","Owner ID","Status","Submitted","Reviewed by"),registrations.findAll(),
                    r->new Object[]{r.getId(),r.getPetId(),r.getOwnerId(),r.getStatus(),r.getSubmittedDate(),r.getReviewedBy()});
            add(wb,"Service Requests",List.of("ID","Dog ID","Owner ID","Service","Status","Submitted","Decision by"),requests.findAll(),
                    r->new Object[]{r.getId(),r.getPetId(),r.getOwnerId(),r.getServiceName(),r.getStatus(),r.getSubmittedDate(),r.getDecidedBy()});
            add(wb,"Adoptions",List.of("ID","Applicant ID","Shelter dog ID","Status","Submitted"),adoptions.findAll(),
                    a->new Object[]{a.getId(),a.getApplicantOwnerId(),a.getShelterPetId(),a.getStatus(),a.getSubmittedDate()});
            add(wb,"Action History",List.of("ID","Date and time","User","Action","Module","Reference","Description"),audit.findTop500ByOrderByOccurredAtDesc(),
                    a->new Object[]{a.getId(),a.getOccurredAt(),a.getUserName(),a.getAction(),a.getModule(),a.getRefId(),a.getDescription()});
            wb.write(response.getOutputStream());
        }
    }

    private static <T> void add(Workbook wb, String name, List<String> headers, List<T> rows, Function<T,Object[]> values) {
        Sheet sheet=wb.createSheet(name); CellStyle head=wb.createCellStyle();
        Font font=wb.createFont(); font.setBold(true); head.setFont(font); head.setFillForegroundColor(IndexedColors.DARK_RED.getIndex());
        head.setFillPattern(FillPatternType.SOLID_FOREGROUND); font.setColor(IndexedColors.WHITE.getIndex());
        Row h=sheet.createRow(0); for(int i=0;i<headers.size();i++){Cell c=h.createCell(i);c.setCellValue(headers.get(i));c.setCellStyle(head);}
        int n=1; for(T item:rows){Row row=sheet.createRow(n++);Object[] vals=values.apply(item);for(int i=0;i<vals.length;i++)row.createCell(i).setCellValue(vals[i]==null?"":String.valueOf(vals[i]));}
        sheet.createFreezePane(0,1); for(int i=0;i<headers.size();i++) sheet.setColumnWidth(i, Math.min(50, Math.max(14, headers.get(i).length()+3))*256);
    }
}
