package gov.samhsa.c2s.c2ssofapi.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageDto<T> {
    int size;
    double totalNumberOfPages;
    int currentPage; //Starts with 1 if elements are present
    int currentPageSize; //Will always be less than or equal to "size", for example: a last page
    boolean hasNextPage;
    boolean hasPreviousPage;
    boolean firstPage;
    boolean lastPage;
    int totalElements;
    boolean hasElements;
    List<T> elements;
    // TODO: Add sort related info


    public PageDto(List<T> elements, int size, double totalNumberOfPages, int currentPage, int currentPageSize, int totalElements) {
        this.elements = elements;
        this.size = size;
        this.totalNumberOfPages = totalNumberOfPages;
        this.currentPage = currentPage;
        this.currentPageSize = currentPageSize;
        this.totalElements = totalElements;
        this.hasPreviousPage = currentPage > 1 && currentPage <= totalNumberOfPages;
        this.hasNextPage = currentPage >= 1 && currentPage < totalNumberOfPages;
        this.firstPage = currentPage == 1;
        this.lastPage = currentPage == totalNumberOfPages;
        this.hasElements = this.getElements().size() > 0;
    }
}
