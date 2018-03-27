% Thesis class definition. Modified by Benjamin Nickolls
% (msc47bgn@bham.ac.uk) for Birmingham Univesity's guidelines from:

% University of Birmingham Thesis LaTex2e Class definition.
% Written by Benjamin Nickolls 2007, based on
% the thesis.sty written at UofT by fpitt.
% (bernecky@acm.org)

% Revised by Joe Groocock (2017) for use with articles
% Revised by Chris Lane (2018) to fix image warnings and use two column

\NeedsTeXFormat{LaTeX2e}[1995/12/01]
\ProvidesClass{bhamarticle}[1997/07/15 University of Birmingham Article class
definition]
\RequirePackage{setspace,graphicx}

% The following options are what you get if you do not specify ANY options.
% E.g., \documentclass{utthesis}
% Basically, it is your camera copy.
\ExecuteOptions{a4paper,oneside,12pt,twocolumn,final,openany}

% If you want a cheap-and-cheerful draft copy, try options such
%% \documentclass[draft,twoside,11pt]{utthesis}

% Set up handling of "draft" option.
\DeclareOption{draft}{%
    \PassOptionsToPackage{all}{draftcopy}%
    \PassOptionsToClass{draft}{article}%
    \AtEndOfClass{%
        \RequirePackage{draftcopy}%
        % I wanted to print timestamp of each file, but I don't see
        % how to do that...
        \draftcopyName{DRAFT: \today}{60 } % text for overprinting if in "draft" mode
    }%
}

% Pass ALL options to article.cls.
\DeclareOption*{%
    \PassOptionsToClass{\CurrentOption}{article}
}

\ProcessOptions


\LoadClass{article}
% Set up environments for thesis sections.

% clearemptydoublepage from page 92 of The LaTeX Companion 1993.
\newcommand{\clearemptydoublepage}{\newpage{\pagestyle{empty}\cleardoublepage}}
\newcommand{\keywords}[1]{\textbf{\textit{Keywords---}} #1}

\renewenvironment{abstract}%
{\clearemptydoublepage
    \onecolumn
    \thispagestyle{plain}
    \begin{center}
        \vspace*{1.5cm}
        \section*{\Large \bfseries Abstract}
        \vspace{0.5cm}
    \end{center}
    \begin{quote}
}{\newpage\end{quote}\twocolumn}

\newenvironment{acknowledgements}%
{\clearemptydoublepage
    \onecolumn
    \thispagestyle{plain}
    \begin{center}
        \vspace*{1.5cm}
        \section*{\Large \bfseries Acknowledgements}
        \vspace*{0.5cm}
    \end{center}
    \begin{quote}
}{\newpage\end{quote}\twocolumn}

\newenvironment{dedication}%
{\clearemptydoublepage
    \onecolumn
    \thispagestyle{plain}
    \begin{center}
        \vspace*{1.5cm}
        \section*{\LARGE}
        \vspace*{0.5cm}
    \end{center}
    \begin{verse}
}{\newpage\end{verse}\twocolumn}

\newenvironment{preliminary}%
{\pagestyle{plain}\pagenumbering{roman}}%
{\pagenumbering{arabic}}

% Default values for title page.

%% To produce output with the desired line spacing, the argument of
%% \spacing should be multiplied by 5/6 = 0.8333, so that 1 1/2 spaced
%% corresponds to \spacing{1.5} and double spaced is \spacing{1.66}.
\def\normalspacing{1.25} % default line spacing

\newcommand{\department}[1]{\ifx\empty#1\else\gdef\@department{#1}\fi}
\newcommand{\degree}[1]{\ifx\empty#1\else\gdef\@degree{#1}\fi}
\renewcommand{\author}[1]{\ifx\empty#1\else\gdef\@author{#1}\fi}
\newcommand{\studentid}[1]{\ifx\empty#1\else\gdef\@studentid{#1}\fi}
\newcommand{\supervisor}[1]{\ifx\empty#1\else\gdef\@supervisor{#1}\fi}
\renewcommand{\title}[1]{\ifx\empty#1\else\gdef\@title{#1}\fi}
\newcommand{\gyear}[1]{\ifx\empty#1\else\gdef\@gyear{#1}\fi}

    \global\def\@department{\mbox{}}
    \global\def\@degree{\mbox{}}
    \global\def\@author{\mbox{}}
    \global\def\@studentid{\mbox{}}
    \global\def\@supervisor{\mbox{}}
    \global\def\@title{\mbox{}}
    \global\def\@gyear{\number\year} % Default year is current year


    %% Change \maketitle to follow the SGS guidelines.
    \renewcommand{\maketitle}
    {\begin{titlepage}
            %% Set the line spacing to 1 for the title page.
            \begin{spacing}{1}
                \begin{large}
                    \begin{center}
                        \mbox{}
                        \vfill
                        \begin{sc}
                            \parbox{0.9\textwidth}{\Huge\center{\@title}\\}
                        \end{sc}
                        \vfill
                        {\@author}\\
                        {\@studentid}\\
                        {\@degree}\\
                        Supervisor: {\@supervisor}\\
                        \vspace*{0.4em}
                        \includegraphics[width=9em,height=9em]{img/crest.eps}\\
                        \vspace*{0.5em}
                        School of {\@department}\\
                        University of Birmingham\\
                        {\@date}\\
                        \vfill
                        Copyright {\copyright} {\@gyear} School of {\@department}, University of Birmingham\\
                        \vspace*{0.5em}
                    \end{center}
                \end{large}
            \end{spacing}
        \end{titlepage}
    }%\maketitle


    %% Footnotes are always single-spaced.
    %%\let\@@footnotetext=\@footnotetext
    %%\def\@footnotetext{\begin{spacing}{1}\@@footnotetext\end{spacing}}

    %% Define the "thesis" page style.
    \if@twoside % If two-sided printing.
        \def\ps@thesis{\let\@mkboth\markboth
            \def\@oddfoot{}
            \let\@evenfoot\@oddfoot
            \def\@oddhead{
                {\sc\rightmark} \hfil \rm\thepage
            }
            \def\@evenhead{
                \rm\thepage \hfil {\sc\leftmark}
            }
            \def\chaptermark##1{\markboth{\ifnum \c@secnumdepth >\m@ne
        \@chapapp\ \thechapter. \ \fi ##1}{}}
        \def\sectionmark##1{\markright{\ifnum \c@secnumdepth >\z@
\thesection. \ \fi ##1}}}
\else % If one-sided printing.
\def\ps@thesis{\let\@mkboth\markboth
    \def\@oddfoot{}
    \def\@oddhead{
        {\sc\rightmark} \hfil \rm\thepage
    }
    \def\chaptermark##1{\markright{\ifnum \c@secnumdepth >\m@ne
\@chapapp\ \thechapter. \ \fi ##1}}}
\fi

\pagestyle{plain}
% Set up page layout.
\setlength{\textheight}{21cm} % Height of the main body of the text
\setlength{\topmargin}{0cm} % 1'' margin on top of page
\setlength{\headsep}{1.5cm}  % space between header and top of body
\addtolength{\headsep}{-\headheight} % See The LaTeX Companion, p 85
\setlength{\footskip}{1cm}  % space between footer and bottom of body
\setlength{\textwidth}{14cm} % width of the body of the text
\setlength{\oddsidemargin}{2cm} % 2cm margin on the left for odd pages
\setlength{\evensidemargin}{-1cm} % 2cm margin on the right for even pages

% Marginal notes
\setlength{\marginparwidth}{.75in} % width of marginal notes
\setlength{\marginparsep}{.125in} % space between marginal notes and text

% Make each page fill up the entire page. comment this out if you prefer.
\flushbottom

\setcounter{tocdepth}{2} % Number the subsubsections
