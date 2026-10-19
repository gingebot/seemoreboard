# Feature 014 — Alternative Keys Improvement (SeeMoreBoard)

## Summary
Enhance SeeMoreBoard’s numeric and symbol layouts by aligning them with Gboard’s structure, adding missing essential characters, and introducing the extended symbol menu. Maintain SeeMoreBoard’s accessibility‑first design, zoom/pan architecture, and contextual IME action key. The “1234” numberpad view is **not** included in this feature.

---

## Objectives
- Add missing essential keys (`'`, `=`, etc.)
- Reformat the **?123** layout to match Gboard’s structure
- Add the **extended symbol menu** (second symbol page)
- Ensure correct bottom‑row behaviour (ABC, space, contextual action key)
- Maintain SeeMoreBoard’s zoom/pan architecture and accessibility spacing
- Do **not** add the “1234” keypad yet

---

# Primary “?123” Layout (Final)

### Row 1 — Numbers
1   2   3   4   5   6   7   8   9   0

Code

### Row 2 — Common Symbols
@   #   £   _   &   -   +   (   )   /

Code

### Row 3 — Punctuation & Operators
=\<   *   '   "   :   ;   !   ?   ⌫

Code
- **=\<** = symbol‑menu trigger  
- **⌫** = backspace

### Row 4 — Bottom Row
ABC   ,   space   .   return/search/etc

Code
- “return/search/etc” = contextual IME action key (Enter / Search / Go / Send)

---

# Extended Symbol Menu Layout (Final)

### Row 1
~   |   √   π   ×   §   Δ

Code

### Row 2
€   ¥   $   ¢   ^   {   }   \

Code

### Row 3
?123   %   ©   ™   ✓   [   ]   ⌫

Code
- **?123** returns to the primary symbol page  
- **⌫** = backspace

### Row 4
ABC   <   space   >   return/search/etc

Code
- “return/search/etc” = contextual IME action key

---

# Behaviour Requirements

### Layout Switching
- **ABC** → alphabetic layout  
- **?123** → primary numeric/symbol layout  
- **=\<** → extended symbol menu  
- **return/search/etc** → contextual IME action key (unchanged)

### Zoom & Pan
- All keys scale uniformly with existing zoom architecture  
- Spacing remains proportional  
- No layout shifting between pages  
- Panning behaviour identical to main keyboard

### Accessibility
- High‑contrast themes apply to both symbol pages  
- Key shapes remain consistent  
- Hitboxes remain rectangular and predictable  
- All symbols must remain legible at maximum zoom

---

# Implementation Requirements

### Add New Key Definitions
Add key entries for:
'   =   ~   |   √   π   ×   §   Δ
€   ¥   $   ¢   ^   {   }   \
%   ©   ™   ✓   [   ]   <   >

Code

### Add New Layout Objects
Implement two new layouts:
- `LayoutNumericSymbols` (primary ?123 page)
- `LayoutExtendedSymbols` (symbol menu)

### Bottom Row Integration
Ensure bottom row uses existing IME action key logic:
- Alphabet switch  
- Comma  
- Space  
- Period  
- Contextual action key  

---

# Exclusions
- **Do NOT** implement the “1234” numberpad view  
- No changes to emoji or language switch keys  
- No changes to gesture typing or long‑press menus (future features)

---

# Deliverables
- Updated key definitions  
- Updated layout JSON/Kotlin objects  
- Updated switching logic  
- Visual verification across:
  - Phone layout  
  - Tablet layout  
  - Foldable layout  
- Accessibility verification (high‑contrast mode)