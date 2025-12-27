package tn.pi.back.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tn.pi.back.dto.ChangePasswordDTO;
import tn.pi.back.dto.RegisterDTO;
import tn.pi.back.dto.UpdateProfileDTO;
import tn.pi.back.dto.UserResponseDTO;
import tn.pi.back.exception.ResourceNotFoundException;
import tn.pi.back.exception.UnauthorizedException;
import tn.pi.back.model.User;
import tn.pi.back.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'email: " + email));
        
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .disabled(!user.isEnabled())
                .build();
    }
    
    @Transactional
    public UserResponseDTO register(RegisterDTO registerDTO) {
        if (userRepository.existsByEmail(registerDTO.getEmail())) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà");
        }
        
        User user = User.builder()
                .email(registerDTO.getEmail())
                .password(passwordEncoder.encode(registerDTO.getPassword()))
                .firstName(registerDTO.getFirstName())
                .lastName(registerDTO.getLastName())
                .phoneNumber(registerDTO.getPhoneNumber())
                .role(registerDTO.getRole())
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        User savedUser = userRepository.save(user);
        log.info("Nouvel utilisateur enregistré: {}", savedUser.getEmail());
        
        return mapToResponseDTO(savedUser);
    }
    
    @Transactional
    public UserResponseDTO updateProfile(Long userId, UpdateProfileDTO updateDTO, String currentUserEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        
        // Vérifier que l'utilisateur modifie son propre profil ou est un admin
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur actuel non trouvé"));
        
        if (!user.getId().equals(currentUser.getId()) && currentUser.getRole() != tn.pi.back.model.Role.ADMIN) {
            throw new UnauthorizedException("Vous n'êtes pas autorisé à modifier ce profil");
        }
        
        // Vérifier si l'email est déjà utilisé par un autre utilisateur
        if (updateDTO.getEmail() != null && !updateDTO.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(updateDTO.getEmail())) {
                throw new RuntimeException("Cet email est déjà utilisé");
            }
            user.setEmail(updateDTO.getEmail());
        }
        
        if (updateDTO.getFirstName() != null) {
            user.setFirstName(updateDTO.getFirstName());
        }
        if (updateDTO.getLastName() != null) {
            user.setLastName(updateDTO.getLastName());
        }
        if (updateDTO.getPhoneNumber() != null) {
            user.setPhoneNumber(updateDTO.getPhoneNumber());
        }
        
        user.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);
        
        return mapToResponseDTO(updatedUser);
    }
    
    @Transactional
    public void changePassword(Long userId, ChangePasswordDTO changePasswordDTO, String currentUserEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        
        // Vérifier que l'utilisateur modifie son propre mot de passe ou est un admin
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur actuel non trouvé"));
        
        if (!user.getId().equals(currentUser.getId()) && currentUser.getRole() != tn.pi.back.model.Role.ADMIN) {
            throw new UnauthorizedException("Vous n'êtes pas autorisé à modifier ce mot de passe");
        }
        
        // Vérifier le mot de passe actuel
        if (!passwordEncoder.matches(changePasswordDTO.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Le mot de passe actuel est incorrect");
        }
        
        user.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        log.info("Mot de passe modifié pour l'utilisateur: {}", user.getEmail());
    }
    
    @Transactional
    public UserResponseDTO uploadPhoto(Long userId, MultipartFile file, String currentUserEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        
        // Vérifier que l'utilisateur modifie son propre profil ou est un admin
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur actuel non trouvé"));
        
        if (!user.getId().equals(currentUser.getId()) && currentUser.getRole() != tn.pi.back.model.Role.ADMIN) {
            throw new UnauthorizedException("Vous n'êtes pas autorisé à modifier ce profil");
        }
        
        // Supprimer l'ancienne photo si elle existe
        if (user.getPhotoUrl() != null) {
            fileStorageService.deleteFile(user.getPhotoUrl());
        }
        
        // Stocker la nouvelle photo
        String photoUrl = fileStorageService.storeFile(file, userId.toString());
        user.setPhotoUrl(photoUrl);
        user.setUpdatedAt(LocalDateTime.now());
        
        User updatedUser = userRepository.save(user);
        return mapToResponseDTO(updatedUser);
    }
    
    @Transactional
    public UserResponseDTO deletePhoto(Long userId, String currentUserEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        
        // Vérifier que l'utilisateur modifie son propre profil ou est un admin
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur actuel non trouvé"));
        
        if (!user.getId().equals(currentUser.getId()) && currentUser.getRole() != tn.pi.back.model.Role.ADMIN) {
            throw new UnauthorizedException("Vous n'êtes pas autorisé à modifier ce profil");
        }
        
        // Supprimer la photo si elle existe
        if (user.getPhotoUrl() != null) {
            fileStorageService.deleteFile(user.getPhotoUrl());
            user.setPhotoUrl(null);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        }
        
        return mapToResponseDTO(user);
    }
    
    public UserResponseDTO getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        return mapToResponseDTO(user);
    }
    
    public UserResponseDTO getProfileByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        return mapToResponseDTO(user);
    }
    
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    private UserResponseDTO mapToResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .photoUrl(user.getPhotoUrl())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}

