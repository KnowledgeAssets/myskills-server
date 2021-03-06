package com.tsmms.skoop.download;

import com.tsmms.skoop.user.profile.UserProfileDocumentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static java.util.Objects.requireNonNull;

@Api(tags = "Download")
@RestController
@RequestMapping(path = "/download")
public class UserProfileDownloadController {

	private final UserProfileDocumentService userProfileDocumentService;

	public UserProfileDownloadController(UserProfileDocumentService userProfileDocumentService) {
		this.userProfileDocumentService = requireNonNull(userProfileDocumentService);
	}

	@ApiOperation(
			value = "Get anonymous user profile as MS Word (DOCX) file.",
			notes = "Get anonymous user profile as MS Word (DOCX) file."
	)
	@ApiResponses({
			@ApiResponse(code = 200, message = "Successful execution"),
			@ApiResponse(code = 401, message = "Invalid authentication"),
			@ApiResponse(code = 403, message = "Insufficient privileges to access resource"),
			@ApiResponse(code = 404, message = "Resource not found"),
			@ApiResponse(code = 500, message = "Error during execution")
	})
	@GetMapping(path = "/users/{referenceId}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<byte[]> getAnonymousUserProfileDocument(@PathVariable("referenceId") String referenceId) {
		final byte[] userProfileDocument = userProfileDocumentService.getAnonymousUserProfileDocument(referenceId);
		final HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Disposition", "attachment; filename=user-profile.docx");
		return new ResponseEntity<>(userProfileDocument, httpHeaders, HttpStatus.OK);
	}

}
