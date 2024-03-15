; To compile an installer:
;   - Place BEATs.jar in installer\resources  (rename to BEATS.jar if necessary)
;   - Change the AppVersion as necessary 

#define MyAppName "Burning Ember"
#define MyAppVersion "2.2.0"
#define MyAppPublisher "Witmoca"
#define MyAppURL "https://github.com/witmoca/BEATs"
#define MyAppExeName "start.bat"
#define MyAppAssocName MyAppName + " File"
#define MyAppAssocExt ".beats"
#define MyAppAssocKey StringChange(MyAppAssocName, " ", "") + MyAppAssocExt
#define SourceIconFolder "..\src\main\resources\Icons\Logo"
#define LogoFileName "logo.ico"
#define DestinationIconFolder "Icons"

[Setup]
AllowNetworkDrive=no
AllowRootDirectory=no
; NOTE: The value of AppId uniquely identifies this application. Do not use the same AppId value in installers for other applications.
; (To generate a new GUID, click Tools | Generate GUID inside the IDE.)
AppId={{44FFEC95-9C0F-440B-A957-07220E827DD7}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
;AppVerName={#MyAppName} {#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppURL}
AppSupportURL={#MyAppURL}
AppUpdatesURL={#MyAppURL}
ArchitecturesInstallIn64BitMode=x64 arm64 ia64
DefaultDirName={autopf}\BEATs
ChangesAssociations=yes
CloseApplications=yes
DisableProgramGroupPage=yes
OutputDir=.\output
OutputBaseFilename=BEATs_Setup_{#MyAppVersion}
SetupIconFile={#SourceIconFolder}\{#LogoFileName}
Compression=lzma
SolidCompression=yes
UninstallDisplayIcon={app}\{#DestinationIconFolder}\{#LogoFileName}
UsePreviousTasks=yes
WizardStyle=modern

[InstallDelete]
; All files to be deleted before installation
; v1_setup group: delete files made by the izpack installer
Type: filesandordirs; Name: "{app}"; Tasks: v1_setup
Type: files; Name: "C:\ProgramData\Microsoft\Windows\Start Menu\Burning Ember.lnk"; Tasks: v1_setup

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "v1_setup"; Description: "{cm:DeleteV1Files}"; Flags: checkedonce 

[Files]
; Copy the batch file
Source: ".\resources\{#MyAppExeName}"; DestDir: "{app}";
; Copy the .jar file
Source: ".\resources\BEATs.jar"; DestDir: "{app}";
; Copy the logo icon
Source: "{#SourceIconFolder}\{#LogoFileName}"; DestDir: "{app}\{#DestinationIconFolder}"

[Registry]
Root: HKA; Subkey: "Software\Classes\{#MyAppAssocExt}\OpenWithProgids"; ValueType: string; ValueName: "{#MyAppAssocKey}"; ValueData: ""; Flags: uninsdeletevalue
Root: HKA; Subkey: "Software\Classes\{#MyAppAssocKey}"; ValueType: string; ValueName: ""; ValueData: "{#MyAppAssocName}"; Flags: uninsdeletekey
Root: HKA; Subkey: "Software\Classes\{#MyAppAssocKey}\DefaultIcon"; ValueType: string; ValueName: ""; ValueData: "{app}\{#DestinationIconFolder}\{#LogoFileName},0"
Root: HKA; Subkey: "Software\Classes\{#MyAppAssocKey}\shell\open\command"; ValueType: string; ValueName: ""; ValueData: """{app}\{#MyAppExeName}"" ""%1"""
Root: HKA; Subkey: "Software\Classes\Applications\{#MyAppExeName}\SupportedTypes"; ValueType: string; ValueName: "{#MyAppAssocExt}"; ValueData: ""

[Icons]
Name: "{autoprograms}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; IconFileName:"{app}\{#DestinationIconFolder}\{#LogoFileName}"
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; IconFileName:"{app}\{#DestinationIconFolder}\{#LogoFileName}"

[CustomMessages]
DeleteV1Files=Delete the installation files of a BEATs version 1.x.x installation. This option does not delete the settings of the previous installation.
